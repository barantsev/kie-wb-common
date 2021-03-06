/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.workbench.common.screens.impl;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.kie.workbench.common.screens.library.api.index.LibraryFileExtensionIndexTerm;
import org.kie.workbench.common.screens.library.api.index.LibraryValueFileNameIndexTerm;
import org.kie.workbench.common.screens.library.api.index.LibraryValueModuleRootPathIndexTerm;
import org.kie.workbench.common.services.refactoring.backend.server.query.NamedQuery;
import org.kie.workbench.common.services.refactoring.backend.server.query.response.FileDetailsResponseBuilder;
import org.kie.workbench.common.services.refactoring.backend.server.query.response.ResponseBuilder;
import org.kie.workbench.common.services.refactoring.backend.server.query.standard.AbstractFindQuery;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueIndexTerm;
import org.uberfire.ext.metadata.backend.lucene.fields.FieldFactory;

@ApplicationScoped
public class FindAllLibraryAssetsQuery
        extends AbstractFindQuery
        implements NamedQuery {

    public static String NAME = "FindAllLibraryAssetsQuery";

    @Inject
    private FileDetailsResponseBuilder responseBuilder;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Query toQuery(final Set<ValueIndexTerm> terms) {

        checkNotNullAndNotEmpty(terms);

        Query query = buildFromMultipleTerms(terms);
        return query;
    }

    @Override
    public Sort getSortOrder() {
        return new Sort(new SortField(FieldFactory.FILE_NAME_FIELD_SORTED,
                                      SortField.Type.STRING));
    }

    @Override
    public ResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }

    /* (non-Javadoc)
     * @see org.kie.workbench.common.services.refactoring.backend.server.query.NamedQuery#validateTerms(java.util.Set)
     */
    @Override
    public void validateTerms(final Set<ValueIndexTerm> queryTerms)
            throws IllegalArgumentException {

        checkInvalidAndRequiredTerms(queryTerms,
                                     NAME,
                                     new String[]{
                                             LibraryValueModuleRootPathIndexTerm.TERM,
                                             null, // not required
                                             null// not required
                                     },
                                     (t) -> (t instanceof LibraryValueModuleRootPathIndexTerm),
                                     (t) -> (t instanceof LibraryValueFileNameIndexTerm),
                                     (t) -> (t instanceof LibraryFileExtensionIndexTerm)
        );

        checkTermsSize(3,
                       queryTerms);
    }
}
