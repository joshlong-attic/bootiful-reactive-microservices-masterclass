# Surf's Up with Wavefront 

This sample code demonstrates the interactions between numerous services and demonstrates how those services can be observed with the wünderkind observability platform from Tanzu, Wavefront. 

The hypothetical flow is: 

```
`api-gateway` ->(HTTP)-> (
    `payments` -> (Kafka)-> `fulfillment`   
    `payments` -> (HTTP)-> `customer-satisfaction`
    )
  ```
