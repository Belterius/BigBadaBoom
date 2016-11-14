/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transmission;

import java.io.Serializable;

public class TransmissionTurn implements Serializable {
    
    private String actionToDo;
    private Object objectToProcess;
    private Object objectToProcess2;

    public TransmissionTurn(String actionToDo, Object objectToProcess, Object objectToProcess2) {
        this.actionToDo = actionToDo;
        this.objectToProcess = objectToProcess;
        this.objectToProcess2 = objectToProcess2;
    }

    public String getActionToDo() {
        return actionToDo;
    }

    public Object getObjetToProcess() {
        return objectToProcess;
    }
    
     public Object getObjetToProcess2() {
        return objectToProcess2;
    }

    @Override
    public String toString() {
        return "TransmissionTour{" + "ActionToDo=" + actionToDo + ", ObjectToProcess=" + objectToProcess + '}';
    }
    
    
    
    
}
