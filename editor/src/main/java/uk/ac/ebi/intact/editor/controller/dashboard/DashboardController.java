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
package uk.ac.ebi.intact.editor.controller.dashboard;

import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.CvPublicationStatusType;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class DashboardController extends JpaAwareController {

    private LazyDataModel<Publication> allPublications;
    private LazyDataModel<Publication> ownedByUser;
    private LazyDataModel<Publication> reviewedByUser;

    private boolean hideAcceptedAndReleased;

    public DashboardController() {
        hideAcceptedAndReleased = true;
    }

    @SuppressWarnings("unchecked")
    public void loadData( ComponentSystemEvent event ) {
        refreshTables();
    }

    public void refreshTables() {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        final String userId = userSessionController.getCurrentUser().getLogin().toUpperCase();

        String hideAcceptedWhereSql = "";

        if (hideAcceptedAndReleased) {
            hideAcceptedWhereSql = " p.status.identifier <> '"+ CvPublicationStatusType.ACCEPTED.identifier()+"' and "+
                                   "p.status.identifier <> '"+ CvPublicationStatusType.ACCEPTED_ON_HOLD.identifier()+"' and "+
                                   "p.status.identifier <> '"+ CvPublicationStatusType.READY_FOR_RELEASE.identifier()+"' and "+
                                   "p.status.identifier <> '"+ CvPublicationStatusType.RELEASED.identifier()+"' ";
        }

        allPublications = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
                "select p from Publication p" + (hideAcceptedAndReleased ? " where " + hideAcceptedWhereSql : ""), "p", "updated", false);

        ownedByUser = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
                                                                    "select p from Publication p where p.currentOwner.login = '"+userId+"'"+
                                                                    (hideAcceptedAndReleased? " and "+hideAcceptedWhereSql : ""), "p", "updated", false);

        reviewedByUser = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
                                                                    "select p from Publication p where p.currentReviewer.login = '"+userId+"'"+
                                                                    (hideAcceptedAndReleased? " and "+hideAcceptedWhereSql : ""), "p", "updated", false);
    }

    public LazyDataModel<Publication> getAllPublications() {
        return allPublications;
    }

    public LazyDataModel<Publication> getOwnedByUser() {
        return ownedByUser;
    }

    public LazyDataModel<Publication> getReviewedByUser() {
        return reviewedByUser;
    }

    public boolean isHideAcceptedAndReleased() {
        return hideAcceptedAndReleased;
    }

    public void setHideAcceptedAndReleased(boolean hideAcceptedAndReleased) {
        this.hideAcceptedAndReleased = hideAcceptedAndReleased;
    }
}