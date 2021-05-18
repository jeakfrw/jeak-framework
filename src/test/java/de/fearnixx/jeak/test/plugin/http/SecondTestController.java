package de.fearnixx.jeak.test.plugin.http;

import de.fearnixx.jeak.reflect.http.RequestMapping;
import de.fearnixx.jeak.reflect.http.RestController;
import de.fearnixx.jeak.reflect.http.params.QueryParam;
import de.fearnixx.jeak.reflect.http.params.RequestBody;
import de.fearnixx.jeak.service.http.RequestMethod;

@RestController(path = "/test", pluginId = "httptestplugin")
public class SecondTestController {

    @RequestMapping(endpoint = "/hello")
    public DummyObject hello() {
        return new DummyObject("second", 20);
    }

    @RequestMapping(endpoint = "/info/:name")
    public String returnSentInfo(@QueryParam(name = "name") String name) {
        return "second" + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody String string) {
        return "second body " + string;
    }
}
