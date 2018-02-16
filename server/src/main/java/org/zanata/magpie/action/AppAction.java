package org.zanata.magpie.action;

import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;

import static org.zanata.magpie.api.APIConstant.API_CONTEXT;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("appAction")
@ViewScoped
public class AppAction {

    public String getApiUrl() {
        FacesContext context = FacesContext.getCurrentInstance();

        // If this isn't a faces request then just return
        if (context == null) {
            return "";
        }

        HttpServletRequest request =
            (HttpServletRequest) context.getExternalContext().getRequest();

        return request.getRequestURL().toString() + API_CONTEXT;
    }
}
