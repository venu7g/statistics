# Solactive AG Statistics Summary

Solactive, as one of the leading providers of financial indexes, consumes and uses real-time trading prices  of tens of thousands of financial instruments from more than 100 exchanges over the world.  
In order to ensure integrity of our index calculation and proper input data, our operations team needs a  restful API to monitor the incoming prices. The main use case for that API is to provide real-time price  statistics from the last 60 seconds (sliding time interval).  
There will be three APIs:  
    •    The first one is called every time we receive a tick. It is also the sole input of this rest API.  
    •    The second one returns the statistics based on the ticks of all instruments of the last 60 seconds (sliding time interval)  
    •    The third one returns the statistics based on the ticks of one instrument of the last 60 seconds (sliding time interval).  

# All APIs might be called in parallel. Notably; POST "/ticks" might be continuously called before the previous /ticks where finished. 

#                                                        Specifications

  
  
# POST /ticks  
  
Every time a new tick arrives, this endpoint will be called. Body:  
    
            {    
            "instrument": "IBM.N",    
            "price": 143.82,    
            "timestamp": 1478192204000    
            }    
  
where:  
    • 	instrument - a financial instrument identifier (string; list of instruments is not known to our service in advance so we add them dynamically)  
    •    price - current trade price of a financial instrument (double)  
    • 	timestamp - tick timestamp in milliseconds (long; this is not current timestamp)  Returns: Empty body with either 201 or 204:  
    •    201 - in case of success  
    •    204 - if tick is older than 60 seconds  

#  GET /statistics  
  
This is the endpoint with aggregated statistics for all ticks across all instruments, this endpoint has to  execute in constant time and memory (O(1)).

It returns the following statistics based on the ticks which happened in the last 60 seconds (sliding time  interval).  
Returns:    
   
          {  
            "avg": 100,    
            "max": 200,    
            "min": 50,    
            "count": 10    
          }    
where:  
    •    avg is a double specifying the average amount of all tick prices in the last 60 seconds  
    •    max is a double specifying single highest tick price in the last 60 seconds  
    •    min is a double specifying single lowest tick price in the last 60 seconds  
    •    count is a long specifying the total number of ticks happened in the last 60 seconds  
    
# GET /statistics/{instrument_identifier}  
 This is the endpoint with statistics for a given instrument.  
 It returns the statistic based on the ticks with a given instrument identifier happened in the last 60 seconds  (sliding time interval). The response is the same as for the previous endpoint but with instrument specific  statistics.  
# 

#  Implementation Details : 

This Statistics Spring Boot provides restful API to monitor the incoming prices. The main use case for that API is to provide real-time price  statistics from the last 60 seconds (sliding time interval).
Below are the details about build and RESTFul API details and usage of the application.
# Build
    . mvn clean install - Make build, run test and generate jar.
    . mvn spring-boot:run - Start the application on port: 8080.

From terminal using curl :  
```sh
$ curl -X POST localhost:8080/ticks  -H "Content-Type: application/json" -d"{\"instrument\": \"IBM.N", \"price\": 143.82 , \"IBM.N", \"timestamp\": 1478192204000}"
$ curl -X GET localhost:8080/statistics
{count=5, sum=121.210000, min=121.210000, average=121.210000, max=121.210000}
$ curl localhost:8080/statistics/IBM 
{count=1, sum=121.210000, min=121.210000, average=121.210000, max=121.210000}
```

To Post Instrument Ticks :

     POST http://localhost:8080/ticks

     Body : Content-Type : application/json

        {    
           "instrument": "IBM.N",    
           "price": 143.82,    
           "timestamp": 1478192204000    
        } 
    Returns 201 if the Instrument is accepted, 204 if it's old (older than 60 seconds).

To Get statistics By Instrument Id :

     GET http://localhost:8080/statistics/IBM
    
     GET http://localhost:8080/statistics/SAP
    
        {
           "count" : "1", 
           "sum" : "121.210000", 
           "min" : "121.210000", 
           "average" :" 121.210000", 
           "max" : "121.210000"
        }
    
 To Get All statistics :
 
     GET http://localhost:8080/statistics
        {
           "count" : "1", 
           "sum" : "121.210000", 
           "min" : "121.210000", 
           "average" :" 121.210000", 
           "max" : "121.210000"
        }

   