(ns poly.web.xtdb.oiqproc
  (:require
   [honey.sql.helpers :refer [join order-by limit]]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [from select where]]
   [poly.web.xtdb.core :as xtdb-c]
   [xtdb.api :as xt]))

(defn read-walkthroughs
  [db]
  (sql/query
   (-> (select :walk/id
               :floor/id
               :floor/image
               :project/id)
       (from [:core_walkthrough :walk])
       (join [:core_floorplan :floor] [:= :walk/floorplan_id :floor/id])
       (join [:core_project :project] [:= :floor/project_id :project/id])
       (where [:< :walk/id 365865])
       (order-by [:walk/id :asc]))
   db))

(defn read-walkthroughs-for-picking
  [db]
  (sql/query
   (-> (select :walk/id
               :floor/id
               :floor/image
               :project/id)
       (from [:core_walkthrough :walk])
       (join [:core_floorplan :floor] [:= :walk/floorplan_id :floor/id])
       (join [:core_project :project] [:= :floor/project_id :project/id])
       (where [:> :walk/id 365865])
       (order-by [:walk/id :desc])
       (limit 100))
   db))

(defn uuid [] (java.util.UUID/randomUUID))

(defn walk-map->tx
  "Take a walk map from SQL and return a transaction vector for XTDB."
  [walk-map & {:keys [fully-processed?] :or {fully-processed? true}}]
  (let [walk-id (:core-walkthrough/id walk-map)
        floor-id (:core-floorplan/id walk-map)
        floor-image (:core-floorplan/image walk-map)
        project-id (:core-project/id walk-map)
        prefix (str "project_" project-id "/floorplan_" floor-id "/walkthrough_" walk-id "/collect")
        walk-eid (uuid)
        raw-data-id (uuid)
        ai-id (uuid)
        camera-data-id (uuid)
        config-id (uuid)
        deploy-images-id (uuid)
        frames-small-id (uuid)
        gps-picker-id (uuid)
        images-id (uuid)
        osfm-id (uuid)
        pdr-id (uuid)
        pvr-id (uuid)
        processing-walk-id (uuid)
        error-id (uuid)]
    (let [raw-data [::xt/put
                    {:xt/id raw-data-id
                     :walk/id walk-eid
                     :raw-data/video (str prefix "/raw_data/20230721_100910129.mp4")
                     :raw-data/imu (str prefix "/raw_data/20230721_100910129_outputformatimu.txt")
                     :raw-data/framepos (str prefix "/raw_data/20230721_100910129_framepos.txt")}]

          ai [::xt/put
              {:xt/id ai-id
               :walk/id walk-eid
               :ai/results (str prefix "/ai/ai_results.json")}]
          camera-data [::xt/put
                       {:xt/id camera-data-id
                        :walk/id walk-eid
                        :camera-data/frames-small-dir (str prefix "/camera_data/frames_small/")
                        :camera-data/framepos (str prefix "/camera_data/framepos.txt")
                        :camera-data/imu (str prefix "/camera_data/imu.txt")}]
          config [::xt/put
                  {:xt/id config-id
                   :walk/id walk-eid
                   :config/yaml (str prefix "/config/oiq_config.yaml")
                   :config/json (str prefix "/config/oiq_config.json")}]
          deploy-images [::xt/put
                         {:xt/id deploy-images-id
                          :walk/id walk-eid
                          :deploy-images/images-dir (str prefix "/deploy_images/*.jpg")}]
          frames-small [::xt/put
                        {:xt/id frames-small-id
                         :walk/id walk-eid
                         :frames-small/frames-small-dir (str prefix "/frames_small/*.jpg")}]
          gps-picker [::xt/put
                      {:xt/id gps-picker-id
                       :walk/id walk-eid
                       :gps-picker/gps-list (str prefix "/gps_picker/gps_list.txt")
                       :gps-picker/gps-picker-user-id (rand-int 100)
                       :gps-picker/alignment-mode (str prefix "/gps_picker/alignment_mode.json")}]
          images [::xt/put
                  {:xt/id images-id
                   :walk/id walk-eid
                   :images/frames-filter (str prefix "/images/frames_filter.json")
                   :images/frames-filter-spark (str prefix "/images/frames_filter_spark.json")
                   :images/quality (str prefix "/images/quality.json")
                   :images/undistorted-dir (str prefix "/images/undistorted/")}]
          osfm [::xt/put
                {:xt/id osfm-id
                 :walk/id walk-eid
                 :osfm/aligned-reconstructions (str prefix "/osfm/aligned_reconstructions.json")
                 :osfm/reconstruction (str prefix "/osfm/reconstruction.json")
                 :osfm/reconstruction-no-point (str prefix "/osfm/reconstruction_no_point.json")
                 :osfm/recon-quality (str prefix "/osfm/recon_quality.txt")
                 :osfm/tracks (str prefix "/osfm/tracks.csv")}]
          pdr [::xt/put
               {:xt/id pdr-id
                :walk/id walk-eid
                :pdr/path (str prefix "/pdr/pdr_path.png")
                :pdr/shots (str prefix "/pdr/pdr_shots.txt")
                :pdr/speed (str prefix "/pdr/speed.json")
                :pdr/trd (str prefix "/pdr/trd.txt")}]
          pvr [::xt/put
               {:xt/id pvr-id
                :walk/id walk-eid
                :pvr/gc-graph (str prefix "/pvr/gcgraph.pv")
                :pvr/bin-files (str prefix "/pvr/processed/*.bin")
                :pvr/jpg-files (str prefix "/pvr/processed/*.jpg")
                :pvr/mesh-files (str prefix "/pvr/processed/*.ply")
                :pvr/config (str prefix "/pvr/processed/config.json")
                :pvr/ipg-bin (str prefix "/pvr/processed/ipg.bin")
                :pvr/ipg-json (str prefix "/pvr/processed/ipg.json.gz")
                :pvr/logo (str prefix "/pvr/processed/logo.png")}]
          processing-walkthrough [::xt/put
                                  {:xt/id processing-walk-id
                                   :walk/id walk-eid
                                   :processing-walk/floorplan-marked (str prefix "/walkthrough/floorplan_marked.png")
                                   :processing-walk/pvr-walkthrough (str prefix "/walkthrough/pvr_walkthrough.json")
                                   :processing-walk/walkthrough (str prefix "/walkthrough/walkthrough.json")}]
          error [::xt/put
                 {:xt/id error-id
                  :walk/id walk-eid
                  :error/message nil}]
          walkthrough [::xt/put
                       {:xt/id walk-eid
                        :walkthrough/id walk-id
                        :floor/image floor-image}]]
      (if fully-processed?
        [raw-data
         ai
         camera-data
         config
         deploy-images
         frames-small
         gps-picker
         images
         osfm
         pdr
         pvr
         processing-walkthrough
         error
         walkthrough]
        (let [gps-picker [::xt/put
                          {:xt/id gps-picker-id
                           :walk/id walk-eid
                           :gps-picker/gps-picker-user-id nil
                           :gps-picker/gps-list (str prefix "/gps_picker/gps_list.txt")
                           :gps-picker/alignment-mode (str prefix "/gps_picker/alignment_mode.json")}]
              osfm [::xt/put
                    {:xt/id osfm-id
                     :walk/id walk-eid
                     :osfm/reconstruction (str prefix "/osfm/reconstruction.json")
                     :osfm/reconstruction-no-point (str prefix "/osfm/reconstruction_no_point.json")
                     :osfm/recon-quality (str prefix "/osfm/recon_quality.txt")
                     :osfm/tracks (str prefix "/osfm/tracks.csv")}]]
          [raw-data
           camera-data
           config
           frames-small
           gps-picker
           images
           osfm
           pdr
           error
           walkthrough])))))

(defn add-user-id
  [node gps-picking-entity]
  (xt/submit-tx
   node
   [[::xt/put
     (assoc gps-picking-entity :gps-picker/gps-picker-user-id nil)]])
  (xt/sync node))

(defn query-gps-picking-walks
  [node]
  (xt/q
   (xt/db node)
   '{:find [?walk-id
            ?floor-image
            ?frames-small-dir
            ?pdr-shots
            ?osfm-reconstruction-no-point
            ?gps-list]
     :keys [walk-id
            floor-image
            frames-small-dir
            pdr-shots
            osfm-recon-no-point
            gps-list]
     :where [[?walk :walkthrough/id ?walk-id]
             [?walk :floor/image ?floor-image]
             [?walk :xt/id ?eid]
             [?gps-picker :walk/id ?eid]
             [?gps-picker :gps-picker/gps-picker-user-id nil]
             [?gps-picker :gps-picker/gps-list ?gps-list]
             [?frames-small :walk/id ?eid]
             [?frames-small :frames-small/frames-small-dir ?frames-small-dir]
             [?pdr :walk/id ?eid]
             [?pdr :pdr/shots ?pdr-shots]
             [?osfm :walk/id ?eid]
             [?osfm :osfm/reconstruction-no-point ?osfm-reconstruction-no-point]]}))

(defn query-gps-picking-walks-entity
  [db-node]
  (xt/q
   db-node
   '{:find [(pull ?walk [:walkthrough/id
                         :floor/image
                         {(:walk/_id {:as :metadata :into {}})
                          [:gps-picker/gps-list
                           :frames-small/frames-small-dir
                           :pdr/shots
                           :osfm/reconstruction-no-point]}])]
     :where [[?walk :walkthrough/id ?walk-id]
             [?walk :floor/image ?floor-image]
             [?walk :xt/id ?eid]
             [?gps-picker :walk/id ?eid]
             [?gps-picker :gps-picker/gps-picker-user-id nil]
             [?gps-picker :gps-picker/gps-list]
             [?frames-small :walk/id ?eid]
             [?frames-small :frames-small/frames-small-dir]
             [?pdr :walk/id ?eid]
             [?pdr :pdr/shots]
             [?osfm :walk/id ?eid]
             [?osfm :osfm/reconstruction-no-point]]}))

(comment
  (-> (query-gps-picking-walks-entity node) ffirst :metadata))

(defn update-attr
  "update the given attribute and value to each entity in the seq"
  [node entities attr val]
  (doseq [e entities]
    (xt/submit-tx
     node
     [[::xt/put
       (assoc e attr val)]])
    (xt/sync node)))

(comment
  (def walk-ids (->> (query-gps-picking-walks-entity (xt/db node)) (map first) (map :walkthrough/id)))
  (def walk-entities (->> (xt/q (xt/db node) '{:find [(pull ?walk [*])]
                                               :in [[?id ...]]
                                               :where [[?walk :walkthrough/id ?id]]}
                                walk-ids)
                          (map first)))
  (update-attr node walk-entities :floor/image "TEST.png")
  (def update-walks (->> (query-gps-picking-walks-entity (xt/db node)) (map first)))
  (def original-entities (->> (query-gps-picking-walks-entity (xt/db node #inst "2023-08-01")) (map first))))

(defn query-milestones
  [node floor-id]
  (xt/q
   (xt/db node)
   '{:find [(pull ?milestone [*])]
     :in [?floor-id]
     :where [[?milestone :floor/id ?floor-id]]}
   floor-id))

(defn query-gps-picking
  [node]
  (xt/q
   (xt/db node)
   '{:find [(pull ?gps-picker [*])]
     :where [[?gps-picker :gps-picker/gps-picker-user-id nil]]}))

(defn query-walks-missing-gps-user-attr
  [node]
  (xt/q
   (xt/db node)
   '{:find [(pull ?gps-picker [*])]
     :where [[?gps-picker :gps-picker/gps-list]
             (not-join [?gps-picker]
                       [?gps-picker :gps-picker/gps-picker-user-id])]
     :timeout 60000}))

(comment
  (def node (xtdb-c/start-xtdb! :prefix "data/test/dev"))
  (let [db            (sql/init-pool {:dbtype   "postgres"
                                      :dbname   "core_db"
                                      :username "core_user"
                                      :password "core_pass_55%"
                                      :host     "localhost"
                                      :port     5432})
        walks         (read-walkthroughs db)
        picking-walks (read-walkthroughs-for-picking db)]
    (doseq [walk walks]
      (xt/submit-tx
       node
       (walk-map->tx walk))
      (xt/sync node)
      (println "walk" (:core-walkthrough/id walk) "done"))
    (doseq [walk picking-walks]
      (xt/submit-tx
       node
       (walk-map->tx walk :fully-processed? false))
      (xt/sync node)
      (println "walk" (:core-walkthrough/id walk) "done"))
    (sql/close-pool db)
    (println "done ingesting"))
  (def gps-walks (query-gps-picking-walks node))
  (println "Got" (count gps-walks) "walks for picking.")
  ;; (xtdb-c/stop-xtdb! node)
  (println "done"))
