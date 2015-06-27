;;article
{:title "some article title"
 :date "some publish date"
 :text "some article text"
 :url "some article url"
 :users-who-read [123 22 321 929]
 :tags [:some :tags :like :starred]
 :id 1
 :feed-id 1}

;;feed
{:url "some feed url"
 :id 1
 :articles [1 2 3 4 5 9 10 43 21]
 :unread-count 100
 :starred-count 0
 :favico "http://some.url/favicon.ico"
 :name "some feed name"}

;;user
{:email "some@email.address"
 :id 123
 :password "somepassword"
 :salt "somesalt"
 :feeds [{:name "cool stuff" :feeds [123 345 456 789]}
         999
         888
         222
         {:name "web dev" :feeds [111 232 333 444]}]}
