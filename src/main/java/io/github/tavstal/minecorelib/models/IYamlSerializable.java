package io.github.tavstal.minecorelib.models;

public interface IYamlSerializable {

    /**
     * Serializes the object to a YAML string.
     *
     * @return The YAML string representation of the object.
     */
    String SerializeToYaml();

    /**
     * Deserializes the object from a YAML string.
     *
     * @param yaml The YAML string to deserialize the object from.
     */
    void DeserializeFromYaml(String yaml);
}
