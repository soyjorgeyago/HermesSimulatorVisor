package es.us.lsi.hermes.util;

import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;

@RequestScoped
public class MessageProvider {

    private ResourceBundle bundle;

    @Produces
    @MessageBundle
    public ResourceBundle getBundle() {
        if (bundle == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            bundle = context.getApplication().getResourceBundle(context, "bundle");
        }
        return bundle;
    }
}
