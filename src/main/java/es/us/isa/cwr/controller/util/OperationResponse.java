/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.us.isa.cwr.controller.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jdelafuente
 *
 */
public class OperationResponse {

    private Map<String, Object> result;

    public OperationResponse() {
        result = new HashMap<>();
    }

    /**
     * @return the result
     */
    public Map<String, Object> getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    /**
     * @param string
     * @return
     */
    public Object get(String string) {
        return result.get(string);
    }

    /**
     * @param string
     * @param obj
     */
    public void put(String string, Object obj) {
        result.put(string, obj);
    }

    /**
     * @param result
     */
    public void putAll(OperationResponse result) {
        for (String key : result.getResult().keySet()) {
            this.result.put(key, result.getResult().get(key));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entrySet : result.entrySet()) {
            sb.append("\t").append(entrySet.getKey()).append(":" + " ").append(entrySet.getValue()).append("\n");
        }
        return sb.toString();
    }

    public Object remove(String key) {
        return result.remove(key);
    }

}
