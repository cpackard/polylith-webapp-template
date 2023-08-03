(ns poly.web.xtdb.tutorial
  (:require
   [poly.web.xtdb.core :as xtdb-c]
   [xtdb.api :as xt]))

(def manifest
  {:xt/id :manifest
   :pilot-name "Johanna"
   :id/rocket "SB002-sol"
   :id/employee "22910x2"
   :badges "SETUP"
   :cargo ["stereo" "gold fish" "slippers" "secret note"]})

(def node (xtdb-c/start-xtdb!))

(xt/submit-tx node [[::xt/put manifest]])

;; Make sure this transaction has taken effect using sync which ensures that the node's indexes are caught up with the latest transaction.
(xt/sync node)

;; Check that this was successful by asking XTDB to show the whole entity.
(xt/entity (xt/db node) :manifest)

;; You make your way over to the mines on the next shuttle. On your way you decide to get a head start and put the commodities into XTDB.
(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :commodity/Pu
    :common-name "Plutonium"
    :type :element/metal
    :density 19.816
    :radioactive true}]

  [::xt/put
   {:xt/id :commodity/N
    :common-name "Nitrogen"
    :type :element/gas
    :density 1.2506
    :radioactive false}]

  [::xt/put
   {:xt/id :commodity/CH4
    :common-name "Methane"
    :type :molecule/gas
    :density 0.717
    :radioactive false}]])

(xt/sync node)

;; You remember that with XTDB you have the option of adding a valid-time. This comes in useful now as you enter the weeks worth of stock takes for Plutonium.

(comment
  (xt/submit-tx
   node
   [[::xt/put
     {:xt/id :stock/Pu
      :weight-ton 25}]])

  (xt/sync node)

  (xt/entity (xt/db node) :stock/Pu))

(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :stock/Pu
    :commod :commodity/Pu
    :weight-ton 21}
   #inst "2115-02-13T18"]

  [::xt/put
   {:xt/id :stock/Pu
    :commod :commodity/Pu
    :weight-ton 23}
   #inst "2115-02-14T18"]

  [::xt/put
   {:xt/id :stock/Pu
    :commod :commodity/Pu
    :weight-ton 22.2}
   #inst "2115-02-15T18"]

  [::xt/put
   {:xt/id :stock/Pu
    :commod :commodity/Pu
    :weight-ton 24}
   #inst "2115-02-18T18"]

  [::xt/put
   {:xt/id :stock/Pu
    :commod :commodity/Pu
    :weight-ton 24.9}
   #inst "2115-02-19T18"]])

(xt/sync node)

;; You notice that the amount of Nitrogen and Methane has not changed which saves you some time:

(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :stock/N
    :commod :commodity/N
    :weight-ton 3}
   #inst "2115-02-13T18"
   #inst "2115-02-19T18"]

  [::xt/put
   {:xt/id :stock/CH4
    :commod :commodity/CH4
    :weight-ton 92}
   #inst "2115-02-14T18"
   #inst "2115-02-19T18"]])

(xt/sync node)

;; The CEO is impressed with your speed, but a little skeptical that you have done it properly.
;; You gain their confidence by showing them the entries for Plutonium on two different days:
(xt/entity (xt/db node #inst "2115-02-14") :stock/Pu)
(xt/entity (xt/db node #inst "2115-02-18") :stock/Pu)

;; As a parting gift to them you create an easy ingest function so that if they needed to add more commodities to their stock list they could do it fast.
(defn easy-ingest
  "Uses XTDB put transaction to add a vector of documents over a specified node."
  [node docs]
  (xt/submit-tx node
                (vec (for [doc docs]
                       [::xt/put doc])))
  (xt/sync node))

;; It’s a long flight so you refuel, and update your manifest. You have been awarded a new badge, so you add this to your manifest.
(easy-ingest node
             [{:xt/id :manifest
               :pilot-name "Johanna"
               :id/rocket "SB002-sol"
               :id/employee "22910x2"
               :badges ["SETUP" "PUT"]
               :cargo ["stereo" "gold fish" "slippers" "secret note"]}])

;;;; Part 3

;; On your way over to the IPBS office, you input the data in the attachment using the easy ingest function you created on Pluto.
(def data
  [{:xt/id :commodity/Pu
    :common-name "Plutonium"
    :type :element/metal
    :density 19.816
    :radioactive true}

   {:xt/id :commodity/N
    :common-name "Nitrogen"
    :type :element/gas
    :density 1.2506
    :radioactive false}

   {:xt/id :commodity/CH4
    :common-name "Methane"
    :type :molecule/gas
    :density 0.717
    :radioactive false}

   {:xt/id :commodity/Au
    :common-name "Gold"
    :type :element/metal
    :density 19.300
    :radioactive false}

   {:xt/id :commodity/C
    :common-name "Carbon"
    :type :element/non-metal
    :density 2.267
    :radioactive false}

   {:xt/id :commodity/borax
    :common-name "Borax"
    :IUPAC-name "Sodium tetraborate decahydrate"
    :other-names ["Borax decahydrate" "sodium borate" "sodium tetraborate" "disodium tetraborate"]
    :type :mineral/solid
    :appearance "white solid"
    :density 1.73
    :radioactive false}])

(easy-ingest node data)

;; You put together examples and make notes so you can be confident in your lesson.
;; 1. Basic query
;;
;; This basic query is returning all the elements that are defined as :element/metal. The :find clause tells XTDB the variables you want to return.
;; In this case, we are returning the :xt/id due to our placement of element.
(xt/q (xt/db node)
      '{:find [element]
        :where [[element :type :element/metal]]})

;; 2. Quoting
;; The vectors given to the clauses should be quoted. How you do it at this stage is arbitrary.
(=
 (xt/q (xt/db node)
       '{:find [element]
         :where [[element :type :element/metal]]})

 (xt/q (xt/db node)
       {:find '[element]
        :where '[[element :type :element/metal]]})

 (xt/q (xt/db node)
       (quote
        {:find [element]
         :where [[element :type :element/metal]]})))

;; 3. Return the name of metal elements.
;;
;; To find all the names of the commodities that have a certain property, such as :type, you need to use a combination of clauses. Here we have bound the results of type :element/metal to e. Next, we can use the results bound to e and bind the :common-name of them to name. name is what has been specified to be returned and so our result is the common names of all the elements that are metals.
;;
;; One way to think of this is that you are filtering to only get the results that satisfy all the clauses.
(xt/q (xt/db node)
      '{:find [name]
        :where [[e :type :element/metal]
                [e :common-name name]]})

;; 4. More information.
;;
;; You can pull out as much data as you want into your result tuples by adding additional variables to the :find clause.
;;
;; The example below returns the :density and the :common-name values for all entities in XTDB that have values of some kind for both :density and :common-name attributes.
(xt/q (xt/db node)
      '{:find [name rho]
        :where [[e :density rho]
                [e :common-name name]]})

;; 5. Arguments
;;
;; :in can be used to further filter the results. Lets break down what is going down here.
;;
;; First, we are assigning all `:xt/ids` that have a `:type` to `e`:
;; e ← #{[:commodity/Pu] [:commodity/borax] [:commodity/CH4] [:commodity/Au] [:commodity/C] [:commodity/N]}
;;
;; At the same time, we are assigning all the `:types` to `type`:
;; type ← #{[:element/gas] [:element/metal] [:element/non-metal] [:mineral/solid] [:molecule/gas]}
;;
;; Then we assign all the names within `e` that have a `:common-name` to `name`:
;; name ← #{["Methane"] ["Carbon"] ["Gold"] ["Plutonium"] ["Nitrogen"] ["Borax"]}
;;
;; We have specified that we want to get the names out, but not before looking at `:in`
;;
;; For `:in` we have further filtered the results to only show us the names of that have `:type` `:element/metal`.
;;
;; We could have done that before inside the `:where` clause, but using `:in` removes the need for hard-coding inside the query clauses.
;;
;; We pass the value(s) to be used as the third argument to `xt/q`.
(xt/q (xt/db node)
      '{:find [name]
        :where [[e :type type]
                [e :common-name name]]
        :in [type]}
      :element/metal)

;; You give your lesson to the new starters when they return. They are a good audience and follow it well.
;; To check their understanding you set them a task to create a function to aid their daily queries. You are impressed with their efforts.
(defn filter-type
  [type]
  (xt/q (xt/db node)
        '{:find [name]
          :where [[e :common-name name]
                  [e :type type]]
          :in [type]}
        type))

(defn filter-appearance
  [description]
  (xt/q (xt/db node)
        '{:find [name IUPAC]
          :where [[e :common-name name]
                  [e :IUPAC-name IUPAC]
                  [e :appearance appearance]]
          :in [appearance]}
        description))

(filter-type :element/metal)

(filter-appearance "white solid")

;; You update your manifest with the latest badge.
(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :manifest
    :pilot-name "Johanna"
    :id/rocket "SB002-sol"
    :id/employee "22910x2"
    :badges ["SETUP" "PUT" "DATALOG-QUERIES"]
    :cargo ["stereo" "gold fish" "slippers" "secret note"]}]])

(xt/sync node)

;; 4: Bitemporality with XTDB – Neptune Assignment

;; Lyndon gives you some data for a client that you can use as an example. Coast Insurance need to know what kind of cover each customer has and if it was valid at a given time.
;;
;; You show them how to ingest a document using a valid-time so that the information is backdated to when the customer took the cover out.

(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Full}
   #inst "2114-12-03"]])

(xt/sync node)

;; The company needs to know the history of insurance for each cover. You show them how to use the bitemporality of XTDB to do this.

(xt/submit-tx
 node
 [[::xt/put	;; (1)
   {:xt/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Full}
   #inst "2113-12-03" ;; Valid time start
   #inst "2114-12-03"] ;; Valid time end

  [::xt/put  ;; (2)
   {:xt/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Full}
   #inst "2112-12-03"
   #inst "2113-12-03"]

  [::xt/put	;; (3)
   {:xt/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? false}
   #inst "2112-06-03"
   #inst "2112-12-02"]

  [::xt/put ;; (4)
   {:xt/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Promotional}
   #inst "2111-06-03"
   #inst "2112-06-03"]])

;; 1. This is the insurance that the customer had last year. Along with the start valid-time you use an end valid-time so as not to affect the most recent version of the document.

;; 2. This is the previous insurance plan. Again, you use a start and end valid-time.

;; 3. There was a period when the customer was not covered,

;; 4. and before that the customer was on a promotional plan.

(xt/sync node)

;; You now show them a few queries. You know that you can query XTDB as of a given valid-time. This shows the state of XTDB at that time.

;; First you chose a date that the customer had full cover:
(xt/q (xt/db node #inst "2114-01-01")
      '{:find [cover type]
        :where [[e :consumer-id :RJ29sUU]
                [e :cover? cover]
                [e :cover-type type]]})

;; Next you show them a query for a the customer in a time when they had a different type of cover:
(xt/q (xt/db node #inst "2111-07-03")
      '{:find [cover type]
        :where [[e :consumer-id :RJ29sUU]
                [e :cover? cover]
                [e :cover-type type]]})

;; And finally you show them a time when the customer had no cover at all.
(xt/q (xt/db node #inst "2112-07-03")
      '{:find [cover type]
        :where [[e :consumer-id :RJ29sUU]
                [e :cover? cover]
                [e :cover-type type]]})

;; You add the new badge to your manifest
(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :manifest
    :pilot-name "Johanna"
    :id/rocket "SB002-sol"
    :id/employee "22910x2"
    :badges ["SETUP" "PUT" "DATALOG-QUERIES" "BITEMP"]
    :cargo ["stereo" "gold fish" "slippers" "secret note"]}]])

(xt/sync node)

;; 5: Match with XTDB – Saturn Assignment

;; The next shuttle to the CMT office leaves in 5 Earth minutes. While you wait you use the easy ingest function you created on Pluto to put the example data into your system.
(def data
  [{:xt/id :gold-harmony
    :company-name "Gold Harmony"
    :seller? true
    :buyer? false
    :units/Au 10211
    :credits 51}

   {:xt/id :tombaugh-resources
    :company-name "Tombaugh Resources Ltd."
    :seller? true
    :buyer? false
    :units/Pu 50
    :units/N 3
    :units/CH4 92
    :credits 51}

   {:xt/id :encompass-trade
    :company-name "Encompass Trade"
    :seller? true
    :buyer? true
    :units/Au 10
    :units/Pu 5
    :units/CH4 211
    :credits 1002}

   {:xt/id :blue-energy
    :seller? false
    :buyer? true
    :company-name "Blue Energy"
    :credits 1000}])

(easy-ingest node data)

;; You also decide to make some Clojure functions so you can easily show Ubuku the stock and fund levels after the trades.
(defn stock-check
  [company-id item]
  {:result (xt/q (xt/db node)
                 {:find '[name funds stock]
                  :where ['[e :company-name name]
                          '[e :credits funds]
                          ['e item 'stock]]
                  :in '[e]}
                 company-id)
   :item item})

(defn format-stock-check
  [{:keys [result item] :as stock-check}]
  (for [[name funds commod] result]
    (str "Name: " name ", Funds: " funds ", " item " " commod)))

;; You show Ubuku the match operation for a valid transaction. You move 10 units of Methane (:units/CH4) each at the cost of 100 credits to Blue Energy:
(xt/submit-tx
 node
 [[::xt/match
   :blue-energy
   {:xt/id :blue-energy
    :seller? false
    :buyer? true
    :company-name "Blue Energy"
    :credits 1000}]
  [::xt/put
   {:xt/id :blue-energy
    :seller? false
    :buyer? true
    :company-name "Blue Energy"
    :credits 900
    :units/CH4 10}]

  [::xt/match
   :tombaugh-resources
   {:xt/id :tombaugh-resources
    :company-name "Tombaugh Resources Ltd."
    :seller? true
    :buyer? false
    :units/Pu 50
    :units/N 3
    :units/CH4 92
    :credits 51}]
  [::xt/put
   {:xt/id :tombaugh-resources
    :company-name "Tombaugh Resources Ltd."
    :seller? true
    :buyer? false
    :units/Pu 50
    :units/N 3
    :units/CH4 82
    :credits 151}]])

(xt/sync node)

;; You explain that because the old doc is as expected for both the buyer and the seller that the transaction goes through.
;;
;; You show Ubuku the result of the trade using the function you created earlier:
(format-stock-check (stock-check :tombaugh-resources :units/CH4))
(format-stock-check (stock-check :blue-energy :units/CH4))

;; Ubuku asks if you can show them what would happen if the state of funds in the account of a buyer did not match expectations. You show him a trade where the old doc is not as expected for Encompass trade, to buy 10,000 units of Gold from Gold Harmony.
(xt/submit-tx
 node
 [[::xt/match
   :gold-harmony
   {:xt/id :gold-harmony
    :company-name "Gold Harmony"
    :seller? true
    :buyer? false
    :units/Au 10211
    :credits 51}]
  [::xt/put
   {:xt/id :gold-harmony
    :company-name "Gold Harmony"
    :seller? true
    :buyer? false
    :units/Au 211
    :credits 51}]

  [::xt/match
   :encompass-trade
   {:xt/id :encompass-trade
    :company-name "Encompass Trade"
    :seller? true
    :buyer? true
    :units/Au 10
    :units/Pu 5
    :units/CH4 211
    :credits 100002}]
  [::xt/put
   {:xt/id :encompass-trade
    :company-name "Encompass Trade"
    :seller? true
    :buyer? true
    :units/Au 10010
    :units/Pu 5
    :units/CH4 211
    :credits 1002}]])

(xt/sync node)

;; You explain to Ubuku that this time because you have both match operations in the same transaction, the trade does not go through. The accounts remain the same, even though the failing match was the second operation.
(format-stock-check (stock-check :gold-harmony :units/Au))
(format-stock-check (stock-check :encompass-trade :units/Au))

;; You add the new badge to your manifest
(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :manifest
    :pilot-name "Johanna"
    :id/rocket "SB002-sol"
    :id/employee "22910x2"
    :badges ["SETUP" "PUT" "DATALOG-QUERIES" "BITEMP" "MATCH"]
    :cargo ["stereo" "gold fish" "slippers" "secret note"]}]])

(xt/sync node)

;; As you do so, you check to see if you still have the note that the porter gave you for Kaarlang back on Earth.
(xt/q (xt/db node)
      '{:find [belongings]
        :where [[e :cargo belongings]]
        :in [belongings]}
      "secret note")

;; 6: Delete with XTDB – Jupiter Assignment

;; Kaarlang gives you his client history so you can sync up your XTDB node.
(xt/submit-tx node
              [[::xt/put {:xt/id :kaarlang/clients
                          :clients [:encompass-trade]}
                #inst "2110-01-01T09"
                #inst "2111-01-01T09"]

               [::xt/put {:xt/id :kaarlang/clients
                          :clients [:encompass-trade :blue-energy]}
                #inst "2111-01-01T09"
                #inst "2113-01-01T09"]

               [::xt/put {:xt/id :kaarlang/clients
                          :clients [:blue-energy]}
                #inst "2113-01-01T09"
                #inst "2114-01-01T09"]

               [::xt/put {:xt/id :kaarlang/clients
                          :clients [:blue-energy :gold-harmony :tombaugh-resources]}
                #inst "2114-01-01T09"
                #inst "2115-01-01T09"]])

(xt/sync node)

;; To get a good visual aid, you show Kaarlang how to view his client history. This way you both can see when the clients are deleted.
;;
(xt/entity-history
 (xt/db node #inst "2116-01-01T09")
 :kaarlang/clients
 :desc
 {:with-docs? true})

;; You explain that you are using a snapshot of XTDB with a future valid-time to see the full history.
;; The result shows the names of the clients that have been assigned to Kaarlang since he started at the company in 2110.
;; Next, you delete the whole history of clients by choosing a start and end valid-time that spans thier entire employment time.
(xt/submit-tx
 node
 [[::xt/delete :kaarlang/clients #inst "2110-01-01" #inst "2116-01-01"]])

(xt/sync node)

;; Using the same method as before you show Kaarlang the effect that this operation has had.
(xt/entity-history
 (xt/db node #inst "2115-01-01T08")
 :kaarlang/clients
 :desc
 {:with-docs? true})

;; You point out that there are no longer any documents attached to the transactions.

;; However, should you ever need to retrieve the deleted documents again you can always do so, since these valid time deletions are 'soft'.
(xt/entity-history
 (xt/db node #inst "2115-01-01T08")
 :kaarlang/clients
 :desc
 {:with-docs? true
  :with-corrections? true})

;; 7: Evict with XTDB – The secret mission

;; You are given the data for the people on the ship and sync up your XTDB node. You decide that you are going to embark on this adventure along with them so you add your name to the list.
(xt/submit-tx node
              [[::xt/put
                {:xt/id :person/kaarlang
                 :full-name "Kaarlang"
                 :origin-planet "Mars"
                 :identity-tag :KA01299242093
                 :DOB #inst "2040-11-23"}]

               [::xt/put
                {:xt/id :person/ilex
                 :full-name "Ilex Jefferson"
                 :origin-planet "Venus"
                 :identity-tag :IJ01222212454
                 :DOB #inst "2061-02-17"}]

               [::xt/put
                {:xt/id :person/thadd
                 :full-name "Thad Christover"
                 :origin-moon "Titan"
                 :identity-tag :IJ01222212454
                 :DOB #inst "2101-01-01"}]

               [::xt/put
                {:xt/id :person/johanna
                 :full-name "Johanna"
                 :origin-planet "Earth"
                 :identity-tag :JA012992129120
                 :DOB #inst "2090-12-07"}]])

(xt/sync node)

;; Before you start the eviction process you make a query function so you can see the full results of anything stored in XTDB:
(defn full-query
  [node]
  (xt/q
   (xt/db node)
   '{:find [(pull e [*])]
     :where [[e :xt/id id]]}))

;; You show the others the result:
(full-query node)

;; The XTDB manual said that the evict operation will remove a document entirely. Ilex tells you the only person who wishes to exercise their right to be forgotten is Kaarlang.
(xt/submit-tx node [[::xt/evict :person/kaarlang]])

(xt/sync node)

;; You use your function and see that the transaction was a success.
(full-query node)

;; All the data associated with the specified :xt/id has been removed from the XTDB along with the eid itself.

;; The transaction history is immutable. This means the transactions will never be removed. You assure Ilex that the documents are completely removed from XTDB, you can show this by looking at the history-descending information for each person.
(xt/entity-history (xt/db node)
                   :person/kaarlang
                   :desc
                   {:with-docs? true})

;; 8: Await with XTDB - Kepra-5 Assignment

;; You ingest the known data from the solar system.
(def stats
  [{:body "Sun"
    :type "Star"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 109.3
    :volume 1305700
    :mass 33000
    :gravity 27.9
    :xt/id :Sun}
   {:body "Jupiter"
    :type "Gas Giant"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 10.97
    :volume 1321
    :mass 317.83
    :gravity 2.52
    :xt/id :Jupiter}
   {:body "Saturn"
    :type "Gas Giant"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius :volume
    :mass :gravity
    :xt/id :Saturn}
   {:body "Saturn"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 9.14
    :volume 764
    :mass 95.162
    :gravity 1.065
    :type "planet"
    :xt/id :Saturn}
   {:body "Uranus"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 3.981
    :volume 63.1
    :mass 14.536
    :gravity 0.886
    :type "planet"
    :xt/id :Uranus}
   {:body "Neptune"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 3.865
    :volume 57.7
    :mass 17.147
    :gravity 1.137
    :type "planet"
    :xt/id :Neptune}
   {:body "Earth"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 1
    :volume 1
    :mass 1
    :gravity 1
    :type "planet"
    :xt/id :Earth}
   {:body "Venus"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 0.9499
    :volume 0.857
    :mass 0.815
    :gravity 0.905
    :type "planet"
    :xt/id :Venus}
   {:body "Mars"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 0.532
    :volume 0.151
    :mass 0.107
    :gravity 0.379
    :type "planet"
    :xt/id :Mars}
   {:body "Ganymede"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 0.4135
    :volume 0.0704
    :mass 0.0248
    :gravity 0.146
    :type "moon"
    :xt/id :Ganymede}
   {:body "Titan"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 0.4037
    :volume 0.0658
    :mass 0.0225
    :gravity 0.138
    :type "moon"
    :xt/id :Titan}
   {:body "Mercury"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 0.3829
    :volume 0.0562
    :mass 0.0553
    :gravity 0.377
    :type "planet"
    :xt/id :Mercury}])

(xt/submit-tx node (mapv (fn [stat] [::xt/put stat]) stats))

(xt/sync node)

;; You want to check against the data of the other planets on your node, to see how the gravity from this planet compares, so you write a function to add the new planetary data and query it against the other planets:
(xt/submit-tx
 node
 [[::xt/put
   {:body "Kepra-5"
    :units {:radius "Earth Radius"
            :volume "Earth Volume"
            :mass "Earth Mass"
            :gravity "Standard gravity (g)"}
    :radius 0.6729
    :volume 0.4562
    :mass 0.5653
    :gravity 1.4
    :type "planet"
    :xt/id :Kepra-5}]])

(xt/sync node)

(sort
 (xt/q
  (xt/db node)
  '{:find [g planet]
    :where [[planet :gravity g]]}))
;; Nice, you see that Kepra-5 has gravitational forces stronger than Neptune but weaker than Jupiter.

;; Your task is to make a function that ensures no passport is given before the traveller's data is successfully ingested into XTDB.
(defn ingest-and-query
  [traveller-doc]
  (xt/submit-tx node [[::xt/put traveller-doc]])
  (xt/q
   (xt/db node)
   '{:find [n]
     :where [[id :passport-number n]]
     :in [id]}
   (:xt/id traveller-doc)))

;; You test out your function.
(ingest-and-query
 {:xt/id :origin-planet/test-traveller
  :chosen-name "Test"
  :given-name "Test Traveller"
  :passport-number (java.util.UUID/randomUUID)
  :stamps []
  :penalties []})
;; This strikes you as peculiar - you received no errors from your XTDB node upon submitting, but the ingested traveller doc has not returned a passport number.

;; You are sure your query and ingest syntax is correct, but to check you try running the query again. This time you get the expected result:
(ingest-and-query
 {:xt/id :origin-planet/test-traveller
  :chosen-name "Test"
  :given-name "Test Traveller"
  :passport-number (java.util.UUID/randomUUID)
  :stamps []
  :penalties []})

;; Confused, you open your trusty XTDB manual, skimming through until you hit the page on await-tx:
;; Blocks until the node has indexed a transaction that is at or past the supplied tx. Will throw on timeout. Returns the most recent tx indexed by the node.
;;
;; Of course. Submit operations in XTDB are asynchronous - your query did not return the new data as it had not yet been indexed into XTDB. You decide to rewrite your function using await-tx:
(defn ingest-and-query
  "Ingests the given traveller's document into XTDB, returns the passport
  number once the transaction is complete."
  [traveller-doc]
  (xt/await-tx node
               (xt/submit-tx node [[::xt/put traveller-doc]]))
  (xt/q
   (xt/db node)
   '{:find [n]
     :where [[id :passport-number n]]
     :in [id]}
   (:xt/id traveller-doc)))

;; You run the function again, Changing the traveller-doc so you can see if it’s worked. This time you receive the following:
(ingest-and-query
 {:xt/id :origin-planet/new-test-traveller
  :chosen-name "Testy"
  :given-name "Test Traveller"
  :passport-number (java.util.UUID/randomUUID)
  :stamps []
  :penalties []})
