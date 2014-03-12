package co.realtime.storage.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import co.realtime.storage.ItemAttribute;
import co.realtime.storage.api.ActiveRecordState;
import co.realtime.storage.api.ActiveRecordsCollectionState;
import co.realtime.storage.api.QueryRef;
import co.realtime.storage.async.ActiveRecordStateFuture;
import co.realtime.storage.async.ActiveRecordsCollectionStateFuture;
import co.realtime.storage.ext.StorageException;
import co.realtime.storage.models.ActiveRecord;

public class PatientRecordTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        final PatientRecordSample joaoPatient = new PatientRecordSample();
        joaoPatient.setCpf("1");
        joaoPatient.setName("joao");

        final List<ConnectionInfoSample> connections = new ArrayList<>(0);
        ConnectionInfoSample connection = new ConnectionInfoSample();
        connection.setName("Joao Antunes");
        connections.add(connection);

        connection = new ConnectionInfoSample();
        connection.setName("Joao Carlos");
        connections.add(connection);

        joaoPatient.setConnections(connections);
        joaoPatient.save(null, null).get(); // blocking

        final PatientRecordSample vitorPatient = new PatientRecordSample();
        vitorPatient.setCpf("2");
        vitorPatient.setName("vitor");
        vitorPatient.save(null, null).get(); // blocking

    }

    @AfterClass
    public static void clearData() throws Exception {
        final ActiveRecordsCollectionStateFuture<PatientRecordSample> future = (ActiveRecordsCollectionStateFuture<PatientRecordSample>) PatientRecordSample.fetchAll(PatientRecordSample.class, null, null);
        final ActiveRecordsCollectionState<PatientRecordSample> collectionState = future.get();
        final List<PatientRecordSample> records = collectionState.records();
        for (final PatientRecordSample patient : records) {
            patient.delete(null, null);
        }
    }

    @Test
    public void fetchAllTest() throws InstantiationException, IllegalAccessException, StorageException, InterruptedException, ExecutionException {
        final ActiveRecordsCollectionStateFuture<PatientRecordSample> future = (ActiveRecordsCollectionStateFuture<PatientRecordSample>) PatientRecordSample.fetchAll(PatientRecordSample.class, null, null);
        final ActiveRecordsCollectionState<PatientRecordSample> collectionState = future.get();
        final List<PatientRecordSample> records = collectionState.records();
        assertEquals(2, records.size());
    }

    @Test
    public void fetchAllJoaoPatients() throws InstantiationException, IllegalAccessException, StorageException, InterruptedException, ExecutionException {
        final QueryRef<PatientRecordSample> query = (QueryRef<PatientRecordSample>) PatientRecordSample.createQuery(PatientRecordSample.class);
        query.ref().contains("name", new ItemAttribute("joao"));
        final ActiveRecordsCollectionStateFuture<PatientRecordSample> future = (ActiveRecordsCollectionStateFuture<PatientRecordSample>) PatientRecordSample.executeQuery(query, null, null);
        final ActiveRecordsCollectionState<PatientRecordSample> collectionState = future.get();
        final List<PatientRecordSample> records = collectionState.records();
        assertEquals(1, records.size());

    }

    @Test
    public void aFullCrudTest() throws StorageException, InterruptedException, ExecutionException {

        final String cpf = "200";
        final String name = "joao";

        ActiveRecordStateFuture<ActiveRecord> future = null;

        // create patient
        final PatientRecordSample newPatient = new PatientRecordSample();

        newPatient.setCpf(cpf);
        newPatient.setName(name);

        future = newPatient.save(null, null);
        future.get();

        // fetch created patient
        final PatientRecordSample fetchedPatient = new PatientRecordSample();
        fetchedPatient.setCpf(cpf);

        future = fetchedPatient.fetch(null, null);
        future.get();
        assertEquals("joao", fetchedPatient.getName());

        // update patient
        fetchedPatient.setName("Joao Antunes");
        future = fetchedPatient.save(null, null);
        future.get();

        final PatientRecordSample updatedPatient = new PatientRecordSample();
        updatedPatient.setCpf(cpf);

        future = updatedPatient.fetch(null, null);
        future.get();
        assertEquals("Joao Antunes", updatedPatient.getName());

        // delete patient
        future = updatedPatient.delete(null, null);
        future.get();

        final PatientRecordSample afterDelete = new PatientRecordSample();
        afterDelete.setCpf(cpf);

        future = afterDelete.fetch(null, null);

        final ActiveRecordState<ActiveRecord> state = future.get();
        assertEquals(Boolean.TRUE, Boolean.valueOf(state.hasError()));

    }

}
