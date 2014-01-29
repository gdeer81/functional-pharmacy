import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import java.io.Reader;
import java.io.FileReader;

import datomic.Entity;
import datomic.Connection;
import datomic.Database;
import datomic.Peer;
import datomic.Util;

public class Pharmacist {

    public static void main(String[] args) {

        try {
            System.out.println("Creating and connecting to database...");

            String uri = "datomic:mem://fun-pharm";
            Peer.createDatabase(uri);
            Connection conn = Peer.connect(uri);

            pause();


            System.out.println("Parsing schema edn file and running transaction...");

            Reader schema_rdr = new FileReader("resources/db/schema.edn");
            List schema_tx = (List) Util.readAll(schema_rdr).get(0);
            Object txResult = conn.transact(schema_tx).get();
            System.out.println(txResult);

            pause();


            System.out.println("Parsing seed data edn file and running transaction...");

            Reader data_rdr = new FileReader("resources/db/all-data.edn");
            List data_tx = (List) Util.readAll(data_rdr).get(0);
            data_rdr.close();
            txResult = conn.transact(data_tx).get();

            pause();

            System.out.println("Getting all people from the database");
            Collection results = Peer.q("[:find ?e
                                          :where [?e :person/name]]", conn.db());
            System.out.println(results.size());

            pause();


            System.out.println("Getting first entity id in results, making entity map, displaying keys...");

            results = Peer.q("[:find ?e :where [?e :person/name]]", conn.db());
            Long id = (Long) ((List)results.iterator().next()).get(0);
            Entity entity = conn.db().entity(id);
            System.out.println(entity.keySet());

            pause();


            System.out.println("Displaying the value of the entity's person name...");

            System.out.println(entity.get(":person/name"));

            pause();


            System.out.println("Getting name of each drug...");

            Database db = conn.db();
            for (Object result : results) {
                entity = db.entity(((List) result).get(0));
                System.out.println(entity.get(":drug/name"));
            }

            pause();


            System.out.println("Getting color of each drug...");

            db = conn.db();
            for (Object result : results) {
                entity = db.entity(((List) result).get(0));
                Entity drug = (datomic.Entity) entity.get(":drug/name");
                System.out.println(neighborhood.get(":drug/color"));
            }

            pause();


            System.out.println("Find all prescriptions...");

            results = Peer.q("[:find ?e :where [?e :prescription/patient _]]",
                             conn.db());
            for (Object result : results) System.out.println(((List) result).get(1));

            pause();


            System.out.println("Find all prescriptions for Cam Barker...");

            results = Peer.q("[:find ?e :in $ ?name :where [?p :person/name ?name][?e :prescription/patient ?p]]",
                             conn.db(), "Cam Barker");
            for (Object result : results) System.out.println(result);

            pause();

            System.out.println("find all transactions");

            results = Peer.q("[:find ?when :where [?tx :db/txInstant ?when]]", conn.db());

            pause();

            System.out.println("Sort transactions by time they occurred, then " +
                               "pull out date when seed data load transaction and " +
                               "schema load transactions were executed...");

            List tx_dates = new ArrayList();
            for (Object result : results) tx_dates.add(((List) result).get(0));
            Collections.sort(tx_dates);
            Collections.reverse(tx_dates);
            Date data_tx_date = (Date) tx_dates.get(0);
            Date schema_tx_date = (Date) tx_dates.get(1);

            pause();


        Peer.shutdown(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static final Scanner scanner = new Scanner(System.in);

    private static void pause() {
        if (System.getProperty("NOPAUSE") == null) {
            System.out.println("\nPress enter to continue...");
            scanner.nextLine();
        }
    }
}
