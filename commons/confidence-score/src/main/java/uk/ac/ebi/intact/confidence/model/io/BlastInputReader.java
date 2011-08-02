/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.model.io;

import uk.ac.ebi.intact.bridges.blast.model.BlastInput;

import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Reader strategy for BlastInput objects.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public interface BlastInputReader {

    List<BlastInput> read( File inFile) throws IOException;

    Set<BlastInput> read2Set (File inFile) throws IOException;

    Iterator<BlastInput> iterate(File inFile) throws FileNotFoundException;
}