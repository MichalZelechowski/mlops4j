/*
 *  Copyright 2020 Michał Żelechowski <MichalZelechowski@github.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.mlops4j.storage.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mlops4j.storage.api.exception.ConversionException;
import org.mlops4j.storage.api.exception.DurabilityException;
import org.mlops4j.storage.api.exception.UnexpectedTypeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Michał Żelechowski <MichalZelechowski@github.com>
 */

public class Metadata<T extends Durable<T>> implements Storable {
    private final Map<String, DurabilityEntry<?, ?>> parameters = Maps.newHashMap();
    private Class<? extends ComponentBuilder<T>> builderClass;
    private String builderClassName;
    private ComponentBuilder<T> builder;

    public Metadata(T durable) {
        this(durable.getBuilder());
    }

    public Metadata(ComponentBuilder<T> builder) {
        this.builder = builder;
        this.builderClass = (Class<? extends ComponentBuilder<T>>) builder.getClass();
        this.builderClassName = this.builderClass.getName();
    }

    public Metadata(Class<? extends ComponentBuilder<T>> builderClass) {
        this.builderClassName = builderClass.getName();
        this.builderClass = builderClass;
        this.builder = tryCreatingBuilder();
    }

    public Metadata(String builderClassName) {
        this.builderClassName = builderClassName;
        this.builderClass = tryCreatingBuilderClass();
        this.builder = tryCreatingBuilder();
    }

    public Metadata() {

    }

    @Override
    public void fromBytes(byte[] bytes) throws DurabilityException {
        JSONObject json = new JSONObject(new String(bytes));
        this.fromJSON(json);
    }

    private Metadata<T> fromJSON(JSONObject json) throws DurabilityException {
        this.builderClassName = json.getString("builderClassName");
        this.builderClass = tryCreatingBuilderClass();
        this.builder = tryCreatingBuilder();

        JSONObject parameters = json.getJSONObject("parameters");
        for (String key : parameters.keySet()) {
            JSONObject durabilityEntry = parameters.getJSONObject(key);
            DurabilityType type = durabilityEntry.getEnum(DurabilityType.class, "type");
            Object value = durabilityEntry.get("value");
            DurabilityEntry<?, ?> entry = DurabilityEntry.fromJSON(type, value);
            this.parameters.put(key, entry);
        }
        return this;
    }

    public Metadata<T> withParameter(String name, Object value) throws DurabilityException {
        Preconditions.checkNotNull(value, "Value cannot be null");
        this.parameters.put(name, DurabilityEntry.fromReal(value));
        return this;
    }

    private Class<? extends ComponentBuilder<T>> tryCreatingBuilderClass() {
        try {
            Class<?> clazz = Class.forName(this.builderClassName);
            if (ComponentBuilder.class.isAssignableFrom(clazz)) {
                return (Class<? extends ComponentBuilder<T>>) clazz;
            }
        } catch (ClassNotFoundException e) {
            // TODO log warning, as absence of class is still valid case
        }
        return null;
    }

    private ComponentBuilder<T> tryCreatingBuilder() {
        if (this.builderClass != null) {
            try {
                return this.builderClass.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                // TODO log warning, as differnece in class construction is permittable
            }
        }
        return null;
    }

    public T getDurable() throws DurabilityException {
        if (this.builder == null) {
            throw new DurabilityException(String.format("No builder available for %s", this.builderClassName));
        }

        for (Map.Entry<String, DurabilityEntry<?, ?>> e : this.parameters.entrySet()) {
            String name = e.getKey();
            DurabilityEntry<?, ?> unit = e.getValue();

            Object value = null;
            try {
                value = unit.getValue();
                MethodUtils.invokeMethod(this.builder, name, value);
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new DurabilityException(String.format("Could not find method %s in builder %s", name, builderClassName), noSuchMethodException);
            } catch (ReflectiveOperationException ex) {
                throw new DurabilityException(String.format("Could not invoke method %s in builder %s with value %s", name, builderClassName, unit), ex);
            } catch (NullPointerException nex) {
                throw new DurabilityException(String.format("Encountered null value when trying to invoke %s with value %s on builder %s", name, unit, builderClass), nex);
            } finally {
                if (value instanceof Closeable) {
                    IOUtils.closeQuietly((Closeable) value);
                }
            }
        }
        return this.builder.build();
    }

    @Override
    public byte[] asBytes() {
        JSONObject json = this.asJson();
        return json.toString().getBytes();
    }

    private JSONObject asJson() {
        JSONObject json = new JSONObject();
        json.put("builderClassName", this.builderClassName);
        Map<String, JSONObject> parameters = Maps.transformValues(this.parameters, DurabilityEntry::asJson);
        json.put("parameters", parameters);

        return json;
    }

    public Collection<String> getHashes() {
        Set<String> hashes = Sets.newHashSet();
        for (DurabilityEntry<?, ?> entry : this.parameters.values()) {
            entry.collectHashes(hashes);
        }
        return hashes;
    }

    private enum DurabilityType {
        INTEGER, FLOAT, DOUBLE, STRING, INSTANT, METADATA, BINARY, ARRAY
    }

    @ToString(doNotUseGetters = true)
    private abstract static class DurabilityEntry<STORED, REAL> {
        protected STORED value;

        protected DurabilityEntry(STORED value) {
            this.value = value;
        }

        public abstract DurabilityType getType();

        public REAL getValue() throws DurabilityException {
            return (REAL) this.value;
        }

        public JSONObject asJson() {
            JSONObject result = new JSONObject();
            result.put("type", this.getType());
            result.put("value", value);
            return result;
        }

        public void collectHashes(Set<String> hashes) {
        }

        public static DurabilityEntry<?, ?> fromReal(Object value) throws DurabilityException {
            DurabilityEntry<?, ?> entry;
            if (value instanceof Integer) {
                entry = new IntegerEntry((Integer) value);
            } else if (value instanceof Float) {
                entry = new FloatEntry((Float) value);
            } else if (value instanceof Double) {
                entry = new DoubleEntry((Double) value);
            } else if (value instanceof String) {
                entry = new StringEntry((String) value);
            } else if (value instanceof Instant) {
                entry = new InstantEntry((Instant) value);
            } else if (value instanceof Durable) {
                entry = new DurableEntry((Durable<?>) value);
            } else if (value instanceof Metadata) {
                entry = new DurableEntry<>((Metadata<?>) value);
            } else if (value.getClass().equals(byte[].class)) {
                entry = new BinaryEntry((byte[]) value);
            } else if (value instanceof InputStream) {
                entry = new BinaryEntry((InputStream) value);
            } else if (value instanceof Path) {
                entry = new BinaryEntry((Path) value);
            } else if (value instanceof File) {
                entry = new BinaryEntry((File) value);
            } else if (value instanceof Collection) {
                entry = new ArrayEntry((Collection<?>) value);
            } else {
                throw new DurabilityException(String.format("Not supported type %s for value %s", value.getClass(), value));
            }
            return entry;
        }

        public static DurabilityEntry<?, ?> fromJSON(DurabilityType type, Object value) throws DurabilityException {
            try {
                switch (type) {
                    case INTEGER:
                        return new IntegerEntry((Integer) value);
                    case FLOAT:
                        if (value instanceof Integer) {
                            return new FloatEntry(((Integer) value).floatValue());
                        }
                        return new FloatEntry((Float) value);
                    case DOUBLE:
                        return new DoubleEntry((Double) value);
                    case STRING:
                        return new StringEntry((String) value);
                    case INSTANT:
                        return new InstantEntry((String) value);
                    case METADATA:
                        return new DurableEntry<>((JSONObject) value);
                    case BINARY:
                        return new BinaryEntry((String) value);
                    case ARRAY:
                        return new ArrayEntry((JSONArray) value);
                    default:
                        throw new UnexpectedTypeException(type, type.getClass().getName());
                }
            } catch (ClassCastException ccex) {
                throw new DurabilityException(String.format("Value %s is of unexpected type for stored type %s", value, type), ccex);
            }
        }
    }

    private static class IntegerEntry extends DurabilityEntry<Integer, Integer> {
        protected IntegerEntry(Integer value) {
            super(value);
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.INTEGER;
        }

    }

    private static class FloatEntry extends DurabilityEntry<Float, Float> {
        protected FloatEntry(Float value) {
            super(value);
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.FLOAT;
        }

    }

    private static class DoubleEntry extends DurabilityEntry<Double, Double> {
        protected DoubleEntry(Double value) {
            super(value);
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.DOUBLE;
        }

    }

    private static class StringEntry extends DurabilityEntry<String, String> {
        protected StringEntry(String value) {
            super(value);
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.STRING;
        }

    }

    private static class InstantEntry extends DurabilityEntry<String, Instant> {
        protected InstantEntry(Instant value) {
            super(value.toString());
        }

        public InstantEntry(String value) throws ConversionException {
            super(value);
            getValue();
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.INSTANT;
        }

        public Instant getValue() throws ConversionException {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException ex) {
                throw new ConversionException(value, DurabilityType.INSTANT.name(), ex);
            }
        }

    }

    private static class DurableEntry<T extends Durable<T>> extends DurabilityEntry<Metadata<T>, T> {
        protected DurableEntry(T value) throws DurabilityException {
            this(value.getMetadata());
        }

        protected DurableEntry(Metadata<T> value) {
            super(value);
        }

        public DurableEntry(JSONObject value) throws DurabilityException {
            this(new Metadata<T>().fromJSON(value));
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.METADATA;
        }

        public T getValue() throws DurabilityException {
            return this.value.getDurable();
        }

        @Override
        public JSONObject asJson() {
            JSONObject result = new JSONObject();
            result.put("type", this.getType());
            result.put("value", value.asJson());
            return result;
        }

        public void collectHashes(Set<String> hashes) {
            hashes.addAll(this.value.getHashes());
        }

    }

    private static class BinaryEntry extends DurabilityEntry<String, InputStream> {
        protected BinaryEntry(InputStream value) throws DurabilityException {
            super(null);
            calculateHash(value);
        }


        public BinaryEntry(String value) {
            super(value);
        }

        public BinaryEntry(Path value) throws DurabilityException {
            this(value.toFile());
        }

        public BinaryEntry(File value) throws DurabilityException {
            super(null);
            try (InputStream is = FileUtils.openInputStream(value)) {
                this.calculateHash(is);
            } catch (IOException ioex) {
                throw new DurabilityException(String.format("Cannot read input stream of file %s", value.toString()));
            }
        }

        public BinaryEntry(byte[] value) throws DurabilityException {
            super(null);
            this.calculateHash(new ByteArrayInputStream(value));
        }

        private void calculateHash(InputStream value) throws DurabilityException {
            Hasher hasher = Hashing.sha256().newHasher();
            byte[] buffer = new byte[8196];
            int bytesRead = 0;
            try {
                Path tmpFile = Files.createTempFile("mlops4j", ".bin");
                try (OutputStream ous = new BufferedOutputStream(new FileOutputStream(tmpFile.toFile()))) {
                    do {
                        try {
                            bytesRead = value.readNBytes(buffer, 0, 8196);
                        } catch (IOException e) {
                            throw new DurabilityException(String.format("Cannot read from given stream %s", value));
                        }
                        if (bytesRead > 0) {
                            hasher = hasher.putBytes(buffer, 0, bytesRead);
                            ous.write(buffer, 0, bytesRead);
                        }
                    }
                    while (bytesRead > 0);
                }
                String hash = hasher.hash().toString();
                Files.move(tmpFile, tmpFile.getParent().resolve(hash), StandardCopyOption.REPLACE_EXISTING);
                this.value = hash;
            } catch (IOException ex) {
                throw new DurabilityException("Cannot create temporary file", ex);
            }
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.BINARY;
        }

        @Override
        public InputStream getValue() throws DurabilityException {
            File file = Path.of(FileUtils.getTempDirectoryPath(), this.value).toFile();
            try {
                return IOUtils.buffer(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new DurabilityException(String.format("Cannot find file with content %s", file), e);
            }
        }

        public void collectHashes(Set<String> hashes) {
            hashes.add(this.value);
        }
    }

    private static class ArrayEntry extends DurabilityEntry<DurabilityEntry[], Collection> {

        protected ArrayEntry(Iterable<?> collection) throws DurabilityException {
            super(null);
            List<DurabilityEntry<?, ?>> result = Lists.newLinkedList();
            for (Object item : collection) {
                result.add(DurabilityEntry.fromReal(item));
            }
            this.value = result.toArray(DurabilityEntry[]::new);
        }

        protected ArrayEntry(JSONArray collection) throws DurabilityException {
            super(null);
            List<DurabilityEntry<?, ?>> result = Lists.newLinkedList();
            for (Object item : collection) {
                DurabilityType type = ((JSONObject) item).getEnum(DurabilityType.class, "type");
                Object value = ((JSONObject) item).get("value");
                result.add(DurabilityEntry.fromJSON(type, value));
            }
            this.value = result.toArray(DurabilityEntry[]::new);
        }

        protected ArrayEntry(Object[] array) throws DurabilityException {
            super(null);
            DurabilityEntry<?, ?>[] result = new DurabilityEntry[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = DurabilityEntry.fromReal(array[i]);
            }
            this.value = result;
        }

        @Override
        public DurabilityType getType() {
            return DurabilityType.ARRAY;
        }

        @Override
        public Collection getValue() throws DurabilityException {
            List real = Lists.newLinkedList();
            for (int i = 0; i < this.value.length; i++) {
                real.add(this.value[i].getValue());
            }
            return real;
        }

        @Override
        public JSONObject asJson() {
            JSONObject result = new JSONObject();
            result.put("type", this.getType());
            JSONArray array = new JSONArray();
            for (DurabilityEntry<?, ?> item : this.value) {
                array.put(item.asJson());
            }
            result.put("value", array);
            return result;
        }

        public void collectHashes(Set<String> hashes) {
            for (DurabilityEntry<?, ?> item : this.value) {
                item.collectHashes(hashes);
            }
        }
    }

}
