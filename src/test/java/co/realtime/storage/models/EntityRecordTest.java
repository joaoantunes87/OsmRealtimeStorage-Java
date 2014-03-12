package co.realtime.storage.models;

import static org.junit.Assert.assertEquals;

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

public class EntityRecordTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        final EntityRecordSample rioEntity = new EntityRecordSample();
        rioEntity.setCnes("rio");
        rioEntity.setCap("rio");
        rioEntity.setName("rio");
        rioEntity.save(null, null).get(); // blocking

        final EntityRecordSample saoPauloEntity = new EntityRecordSample();
        saoPauloEntity.setCnes("paulo");
        saoPauloEntity.setCap("paulo");
        saoPauloEntity.setName("paulo");
        saoPauloEntity.save(null, null).get(); // blocking

        // final SignalTaskEndFuture deleteTableTask = new SignalTaskEndFuture();
        // final EntityRecord recordSample = new EntityRecord();
        //
        // recordSample.getTableRef().del(new OnBooleanResponse() {
        //
        // @Override
        // public void run(final Boolean result) {
        //
        // try {
        // deleteTableTask.markSuccess();
        // } catch (final InterruptedException e) {
        // fail(e.toString());
        // }
        //
        // }
        // }, new OnError() {
        //
        // @Override
        // public void run(final Integer errorCode, final String errorMessage) {
        //
        // try {
        // deleteTableTask.markFail();
        // fail(new Error(errorCode, errorMessage).toString());
        // } catch (final InterruptedException e) {
        // fail(e.toString());
        // }
        //
        // }
        //
        // });
        //
        // if (deleteTableTask.get().booleanValue()) {
        //
        // final SignalTaskEndFuture createTableTask = new SignalTaskEndFuture();
        //
        // final StorageDataType primaryKeyType = recordSample.getPrimaryKeyType();
        // final StorageDataType secondaryKeyType = recordSample.getSecondaryKeyType();
        // recordSample.getTableRef().create(recordSample.getPrimaryKeyName(), primaryKeyType, recordSample.getSecondaryKeyName(), secondaryKeyType, StorageProvisionType.CUSTOM, StorageProvisionLoad.CUSTOM, new OnTableCreation() {
        //
        // @Override
        // public void run(final String table, final Double creationDate, final String status) {
        //
        // try {
        // System.out.println(String.format("%s %f %s", table, creationDate, status));
        // createTableTask.markSuccess();
        // } catch (final InterruptedException e) {
        // fail(e.toString());
        // }
        //
        // }
        //
        // }, new OnError() {
        //
        // @Override
        // public void run(final Integer errorCode, final String errorMessage) {
        // try {
        // createTableTask.markFail();
        // fail(new Error(errorCode, errorMessage).toString());
        // } catch (final InterruptedException e) {
        // fail(e.toString());
        // }
        //
        // }
        // });
        //
        // if (createTableTask.get().booleanValue()) {
        // // TODO create some records
        // } else {
        // fail("Couldn't create table");
        // }
        //
        // } else {
        // fail("Couldn't delete table");
        // }

    }

    @AfterClass
    public static void clearData() throws Exception {
        final ActiveRecordsCollectionStateFuture<EntityRecordSample> future = (ActiveRecordsCollectionStateFuture<EntityRecordSample>) EntityRecordSample.fetchAll(EntityRecordSample.class, null, null);
        final ActiveRecordsCollectionState<EntityRecordSample> collectionState = future.get();
        final List<EntityRecordSample> records = collectionState.records();
        for (final EntityRecordSample entity : records) {
            entity.delete(null, null);
        }
    }

    @Test
    public void fetchAllTest() throws InstantiationException, IllegalAccessException, StorageException, InterruptedException, ExecutionException {
        final ActiveRecordsCollectionStateFuture<EntityRecordSample> future = (ActiveRecordsCollectionStateFuture<EntityRecordSample>) EntityRecordSample.fetchAll(EntityRecordSample.class, null, null);
        final ActiveRecordsCollectionState<EntityRecordSample> collectionState = future.get();
        final List<EntityRecordSample> records = collectionState.records();
        assertEquals(2, records.size());
    }

    @Test
    public void fetchAllRioCapEntities() throws InstantiationException, IllegalAccessException, StorageException, InterruptedException, ExecutionException {
        final QueryRef<EntityRecordSample> query = (QueryRef<EntityRecordSample>) EntityRecordSample.createQuery(EntityRecordSample.class);
        query.ref().equals("cap", new ItemAttribute("rio"));
        final ActiveRecordsCollectionStateFuture<EntityRecordSample> future = (ActiveRecordsCollectionStateFuture<EntityRecordSample>) EntityRecordSample.executeQuery(query, null, null);
        final ActiveRecordsCollectionState<EntityRecordSample> collectionState = future.get();
        final List<EntityRecordSample> records = collectionState.records();
        assertEquals(1, records.size());

    }

    @Test
    public void aFullCrudTest() throws StorageException, InterruptedException, ExecutionException {

        final String cnes = "1";
        final String cap = "C1";

        ActiveRecordStateFuture<EntityRecordSample> future = null;

        // create entity
        final EntityRecordSample newEntity = new EntityRecordSample();

        newEntity.setCnes(cnes);
        newEntity.setCap(cap);
        newEntity.setName("Rio");

        future = (ActiveRecordStateFuture<EntityRecordSample>) newEntity.save(null, null);
        future.get();

        // fetch created entity
        final EntityRecordSample fetchedEntity = new EntityRecordSample();
        fetchedEntity.setCnes(cnes);
        fetchedEntity.setCap(cap);

        future = (ActiveRecordStateFuture<EntityRecordSample>) fetchedEntity.fetch(null, null);
        future.get();
        assertEquals("Rio", fetchedEntity.getName());

        // update entity
        fetchedEntity.setName("Rio Updated");
        future = (ActiveRecordStateFuture<EntityRecordSample>) fetchedEntity.save(null, null);
        future.get();

        final EntityRecordSample updatedEntity = new EntityRecordSample();
        updatedEntity.setCnes(cnes);
        updatedEntity.setCap(cap);

        future = (ActiveRecordStateFuture<EntityRecordSample>) updatedEntity.fetch(null, null);
        future.get();
        assertEquals("Rio Updated", updatedEntity.getName());

        // delete entity
        future = (ActiveRecordStateFuture<EntityRecordSample>) updatedEntity.delete(null, null);
        future.get();

        final EntityRecordSample afterDelete = new EntityRecordSample();
        afterDelete.setCnes(cnes);
        afterDelete.setCap(cap);

        future = (ActiveRecordStateFuture<EntityRecordSample>) afterDelete.fetch(null, null);

        final ActiveRecordState<EntityRecordSample> state = future.get();
        assertEquals(Boolean.TRUE, Boolean.valueOf(state.hasError()));

    }
}
