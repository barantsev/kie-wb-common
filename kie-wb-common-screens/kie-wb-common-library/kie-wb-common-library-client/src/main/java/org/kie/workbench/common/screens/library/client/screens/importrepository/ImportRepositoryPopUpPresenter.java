/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.library.client.screens.importrepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.guvnor.common.services.project.client.context.WorkspaceProjectContext;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.screens.library.api.LibraryService;
import org.kie.workbench.common.screens.library.client.util.LibraryPlaces;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.common.HasBusyIndicator;

public class ImportRepositoryPopUpPresenter {

    public interface View extends UberElement<ImportRepositoryPopUpPresenter>,
                                  HasBusyIndicator {

        String getRepositoryURL();

        String getUserName();

        String getPassword();

        void show();

        void hide();

        void showError(final String errorMessage);

        String getLoadingMessage();

        String getNoProjectsToImportMessage();

        String getEmptyRepositoryURLValidationMessage();
    }

    private View view;

    private Caller<LibraryService> libraryService;

    private LibraryPlaces libraryPlaces;

    private WorkspaceProjectContext context;

    @Inject
    public ImportRepositoryPopUpPresenter(final View view,
                                          final LibraryPlaces libraryPlaces,
                                          final WorkspaceProjectContext context,
                                          final Caller<LibraryService> libraryService) {
        this.view = view;
        this.libraryPlaces = libraryPlaces;
        this.context = context;
        this.libraryService = libraryService;
    }

    @PostConstruct
    public void setup() {
        view.init(this);
    }

    public void show() {
        view.show();
    }

    public void importRepository() {
        final String repositoryUrl = view.getRepositoryURL();
        if (isEmpty(repositoryUrl)) {
            view.showError(view.getEmptyRepositoryURLValidationMessage());
            return;
        }

        OrganizationalUnit ou = context.getActiveOrganizationalUnit()
                                       .orElseThrow(() -> new IllegalStateException("Cannot import project without an active organizational unit."));

        view.showBusyIndicator(view.getLoadingMessage());
        libraryService.call((WorkspaceProject project) -> {
                                view.hideBusyIndicator();
                                view.hide();
                                libraryPlaces.goToProject(project);
        }, new DefaultErrorCallback() {
            @Override
            public boolean error(Message message, Throwable throwable) {
                view.hideBusyIndicator();
                view.showError(view.getNoProjectsToImportMessage());
                return false;
            }
        }).importProject(ou, repositoryUrl, view.getUserName(), view.getPassword());
    }

    public void cancel() {
        view.hide();
    }

    public View getView() {
        return view;
    }

    private boolean isEmpty(final String text) {
        return text == null || text.trim().isEmpty();
    }
}
