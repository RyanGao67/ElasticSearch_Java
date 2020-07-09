## Solution:  
### How to run this application:
```
 docker-compose up --build
```
Run the above command, the application and elasticsearch/kibana will be up and running.  

### Task 1 - Create the ElasticSearch Index
Given the information above about the fields in our data set, you can now define the ElasticSearch index template.

Under `/src/main/resources/movies_template.json`, there is an empty JSON file. You must fill this file with the template definition
of the search index that you will work with for the remainder of the challenge.

Once you have filled this file, implement the necessary code to have this index `"movies"` created when the SearchApplication is started. 


### Task 2 - Parsing CSV to JSON
Our data is currently in CSV format, but ElasticSearch accepts indexing requests as JSON data. Therefore, we need to parse this CSV data and 
prepare it to be indexed in ElasticSearch as JSON.

You must implement the `parse` method from the file `CSVToJSONParser`. The method takes a single argument, a string fileName of the file that
contains the data we want to parse. You can assume that you will only read CSV files from the `src/main/resouces` folder. The method should write 
the CSV file to a JSON file in the same folder, so it can be used later.

Once you have implemented the parse method, implement the necessary code to trigger the method when the SearchApplication is started.

>**Hint:** Performance: though the provided CSV is a small data set, treat this as if it were extremely big.

### Task 3 - Bulk Indexing
In order to query documents from ElasticSearch, we must first index them. In the previous task, we parsed our CSV data to a JSON file. Now,
you can read in this file of JSON documents and bulk index the data into your `"movies"` index. 

>**Hint 1**: Ideally, all your data would be loaded into the index before the system starts up.

> **Hint 2**: Once again, performance is very important here.

### Task 4 - Simple Query API
Under `QueryResource`, find and implement the `query` method to return documents that match a simple query clause:
- **GET /api/query**
    - q: query in simple Lucene syntax (e.g. `title:The Lion King`, `averageRating:[7.0 TO 8.5]`)
    - docCount: max number of documents to return; max value can be 100 (i.e. requested for 150, still return only 100)
    
- Assumption:
    - the query provided will always be in valid simple Lucene syntax
    
Expected execution:
```
curl -X GET http://localhost:3000/api/query?q=title:The Lion King&docCount=50
```

Expected result:
```
{
    "results": [
        {
            <movie document #1>
        },
        {
            <movie document #2>
        },
        ...
        {
            <movie document #<docCount>
        }
    ]
}
```

### Task 5 - Top Rated Movie API
Under `QueryResource`, find and implement the `topRated` method to return the top 5 rated movies (just the top 5!) released during a specific time range:
- **GET /api/query/topRated**
    - startDate: (required) the starting date of the bucket; in the format YYYY-MM-DD
    - endDate: (required) the ending date of the bucket; in the format YYYY-MM-DD
    - minVotes: (optional) the minimum number of votes needed to consider the movie; default = 1000

- You can always assume the start date will be before the end date
- You can always assume minVotes will be > 0.

Expected execution:
```
curl -X GET http://localhost:3000/api/query/topRated?startDate=2019-01-01&endDate=2019-02-28
```

Expected result:
```
{
    <top rated doc released between 2019-01-01 and 2019-02-28>
}
```


## Bonus Tasks
The following requirements are purely bonus. You will not be penalized for not completing these. You can pick and choose 
none, any or all of the problems. They are strictly for your enjoyment (and our interest!):

1. Describe the necessary steps to add SSL communication between this client and ElasticSearch.

2. Implement a `POST /api/index` endpoint under `IndexResource.java` that will accept a single movie document to be indexed.

3. Enhance the `GET /api/query` (from Task 4) to take a full ElasticSearch Query Syntax (JSON body).

4. Introduce a [Swagger](https://swagger.io/tools/open-source/open-source-integrations/) docs page to easily execute our queries, rather than through `curl`
