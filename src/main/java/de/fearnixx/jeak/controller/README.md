# REST Service interface
For the service to work, you need to include it in the ``JeakBot`` class:


```java
RestControllerManager restControllerManager = new RestControllerManager(new HashMap<>());
injectionService.injectInto(restControllerManager);

```

## the webserver
There is a jetty server started automatically if a controller is registered. By default the port 8723 is used.

## preparing a controller
To write a controller, you need to use some annotations:
* The class as `@RestController` with the api route for this controller.
* Every method which is supposed to be a controller method has to use `@RequestMapping` with the 
corresponding HTTP-Method and the endpoint for this specific method.
* The endpoints have to start with a leading `/` and end without a `/`.

```java
@RestController(endpoint = "/test")
public class TestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "/hello")
    public String hello() {
        return "hallo";
    }

    @RequestMapping(method =  RequestMethod.GET, endpoint = "/info/:name")
    public String returnSentInfo(@RequestParam(name = "name") String name) {
        return "received " + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody(type = String.class, name = "string") String string) {
        return "this is the body " + string;
    }
}
```

## Register a controller
In order to register a REST-controller, you need to inject the RestControllerManager
and register your Controller.
```java
@inject
private RestControllerManager restControllerManager;
restControllerManager.registerController(TestController.class, testController);
```
