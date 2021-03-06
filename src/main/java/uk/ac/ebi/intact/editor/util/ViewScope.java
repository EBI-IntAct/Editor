/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.util;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import javax.faces.context.FacesContext;
import java.util.Map;


public class ViewScope implements Scope {

    public Object get( String name, ObjectFactory objectFactory ) {

        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();

        Object obj;

        if ( viewMap.containsKey( name ) ) {
            obj = viewMap.get( name );
        } else {
            obj = objectFactory.getObject();
            viewMap.put( name, obj );
        }

        return obj;
    }


    public Object remove( String name ) {
        return FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove( name );
    }


    public String getConversationId() {
        return null;
    }


    public void registerDestructionCallback( String name, Runnable callback ) {
        //Not supported
    }


    public Object resolveContextualObject( String key ) {
        return null;
    }

}
