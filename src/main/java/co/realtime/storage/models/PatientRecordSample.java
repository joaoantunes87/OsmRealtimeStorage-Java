package co.realtime.storage.models;

import java.util.Collection;

import co.realtime.storage.annotations.JsonCollectionStorageProperty;
import co.realtime.storage.annotations.StorageProperty;
import co.realtime.storage.annotations.StorageTable;

/**
 * The Class PatientRecord.
 */
@StorageTable(name = "Patient", primaryKey = "cpf")
public class PatientRecordSample extends ActiveRecord {

    /** The cpf. */
    @StorageProperty(name = "cpf", isPrimaryKey = true)
    private String cpf;

    /** The name. */
    @StorageProperty(name = "name")
    private String name;

    @JsonCollectionStorageProperty(name = "connections", klass = ConnectionInfoSample.class)
    private Collection<ConnectionInfoSample> connections;

    /**
     * Instantiates a new entity record.
     */
    public PatientRecordSample() {
        super();
    }

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(final String cpf) {
        this.cpf = cpf;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Collection<ConnectionInfoSample> getConnections() {
        return this.connections;
    }

    public void setConnections(final Collection<ConnectionInfoSample> connections) {
        this.connections = connections;
    }

}
