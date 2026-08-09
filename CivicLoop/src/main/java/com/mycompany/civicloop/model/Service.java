package civicloop.model;

import java.io.Serializable;
import java.util.UUID;


public class Service implements Creditable, Serializable {
    private String serviceId;
    private String serviceType;  
    private String providerId;
    private boolean isAvailable;  


    public Service(String serviceType, String providerId) {
        this.serviceId = UUID.randomUUID().toString().substring(0, 8);
        this.serviceType = serviceType;
        this.providerId = providerId;
        this.isAvailable = true;
    }


}


