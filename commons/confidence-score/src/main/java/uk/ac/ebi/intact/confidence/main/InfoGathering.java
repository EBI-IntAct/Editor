/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.GoFilter;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever;
import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.main.exception.InfoGatheringException;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.Report;
import uk.ac.ebi.intact.confidence.util.DataMethods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

/**
 * Retreives the high confidence ans medium confidence set form the DB.
 * (Step 1)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class InfoGathering {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( InfoGathering.class );
    private ExpansionStrategy expansion;

    public InfoGathering (){
        this( new SpokeExpansion());
    }

    public InfoGathering( ExpansionStrategy expansion){
        this.expansion = expansion;
    }

   public Report retrieveHighConfidenceAndMediumConfidenceSetWithAnnotations( File workDir) throws Exception {
       IntactDbRetriever intactdb = new IntactDbRetriever( workDir, expansion);
       File hcFile = new File(workDir, "highconf_set.txt");
       File mcFile = new File(workDir, "medconf_set.txt");
       Report report = new Report(hcFile, mcFile);
       intactdb.readConfidences(report);
       return report;
   }
    
    /**
     *
     * @param workDir : must contain a highconf_set.txt and a medconf_set.txt file
     * @param fastaFile : for the set of proteins the low confidences will be generated form.
     * @param nr : optional, if n >0 n low confidence interactions will be generated, if n<=0 will generate as many low confidences as high confidences found
     */
    public File retrieveLowConfidenceSet(File workDir, File fastaFile, int nr) throws InfoGatheringException {
          return generateLowconf( workDir, fastaFile, nr);
    }

      public File retrieveLowConfidenceSet(Report report, File fastaFile, int nr) throws InfoGatheringException {
          return generateLowconf( report, fastaFile, nr);
    }

    public void retrieveLowConfidenceSetAnnotations(Report report)throws InfoGatheringException {//File workDir, File lowconfFile) throws InfoGatheringException {
        try {
            BinaryInteractionSet lowConf = new BinaryInteractionSet( report.getLowconfFile().getPath() );
//            File dirForAttrib = report.getLowconfFile().getParentFile(); // new File( workDir, "DataRetriever" );
            writeIpGoForLc( lowConf.getAllProtNames(), report );
        } catch ( IOException e ) {
            throw new InfoGatheringException( e);
        }
    }



     protected File generateLowconf( File workDir, File inFile, int nr ) throws InfoGatheringException {
        DataMethods dm = new DataMethods();
        if ( !inFile.exists() ) {
            throw new RuntimeException( inFile.getAbsolutePath() );
        }
        Set<String> yeastProteins = dm.readFastaToProts( inFile, null );
        try {
            BinaryInteractionSet highConfBiSet = new BinaryInteractionSet( workDir.getPath() + "/highconf_set.txt" );
            BinaryInteractionSet medConfBiSet = new BinaryInteractionSet( workDir.getPath() + "/medconf_set.txt" );
            Collection<ProteinPair> all = highConfBiSet.getSet();
            all.addAll( medConfBiSet.getSet() );
            BinaryInteractionSet forbidden = new BinaryInteractionSet( all );
            BinaryInteractionSet lowConf = dm.generateLowConf( yeastProteins, forbidden, nr );
            File lcFile = new File( workDir.getPath(), "lowconf_set.txt" );
            dm.export( lowConf, lcFile );
            return lcFile;
        } catch ( IOException e ) {
            throw new InfoGatheringException( e);
        }
    }

     protected File generateLowconf( Report report, File inFile, int nr ) throws InfoGatheringException {
        DataMethods dm = new DataMethods();
        if ( !inFile.exists() ) {
            throw new RuntimeException( inFile.getAbsolutePath() );
        }
        Set<String> yeastProteins = dm.readFastaToProts( inFile, null );
        try {
            BinaryInteractionSet highConfBiSet = new BinaryInteractionSet(report.getHighconfFile().getPath() );
            BinaryInteractionSet medConfBiSet = new BinaryInteractionSet( report.getMedconfFile().getPath());
            Collection<ProteinPair> all = highConfBiSet.getSet();
            all.addAll( medConfBiSet.getSet() );
            BinaryInteractionSet forbidden = new BinaryInteractionSet( all );
            BinaryInteractionSet lowConf = dm.generateLowConf( yeastProteins, forbidden, nr );
            File lcFile = new File( report.getMedconfFile().getParentFile(), "lowconf_set.txt" );
            dm.export( lowConf, lcFile );
            report.setLowconfFile( lcFile );
            return lcFile;
        } catch ( IOException e ) {
            throw new InfoGatheringException( e);
        }
    }


    private void writeIpGoForLc( Set<String> lowConfProt, Report report ) throws InfoGatheringException {//File dirForAttrib ) throws InfoGatheringException {
           UniprotDataRetriever uniprot = new UniprotDataRetriever();
           try {
               report.setLowconfGOFile( new File(report.getLowconfFile().getParentFile(),"lowconf_set_go.txt") );
               report.setLowconfIpFile( new File(report.getLowconfFile().getParentFile(),"lowconf_set_ip.txt") );
               report.setLowconfSeqFile( new File(report.getLowconfFile().getParentFile(),"lowconf_set_seq.txt") );

               writeGo( uniprot, lowConfProt, new FileWriter( report.getLowconfGOFile() ));
               writeIp( uniprot, lowConfProt, new FileWriter( report.getLowconfIpFile() ) );
               writeSeq( uniprot, lowConfProt, new FileWriter( report.getLowconfSeqFile() ) );
           } catch ( IOException e ) {
               throw new InfoGatheringException( e);
           }

       }

       private void writeIp( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) throws InfoGatheringException {
           try {
               for ( String ac : lowConfProt ) {
                   Set<Identifier> ips = uniprot.getIps( new UniprotAc( ac ) );
                   fileWriter.append( ac + "," );
                   for ( Identifier ipId : ips ) {
                       fileWriter.append( ipId.getId() + "," );
                   }
                   fileWriter.append( "\n" );
               }
               fileWriter.close();
           } catch ( IOException e ) {
                throw new InfoGatheringException( e);
           }
       }

       private void writeGo( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) throws InfoGatheringException {
           try {
               for ( String ac : lowConfProt ) {
                   Set<Identifier> gos = uniprot.getGos( new UniprotAc( ac ) );
                   GoFilter.filterForbiddenGos( gos);
                   fileWriter.append( ac + "," );
                   for ( Identifier goId : gos ) {
                       fileWriter.append( goId.getId() + "," );
                   }
                   fileWriter.append( "\n" );
               }
               fileWriter.close();
           } catch ( IOException e ) {
                throw new InfoGatheringException( e);
           }
       }

       private void writeSeq( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) throws InfoGatheringException {
           for ( String ac : lowConfProt ) {
               Sequence seq = uniprot.getSeq( new UniprotAc( ac ) );
               if ( seq != null ) {
                   print( ac, seq, fileWriter );
               } else {
                   if ( log.isInfoEnabled() ) {
                       log.info( "No Sequence found for ac: " + ac );
                   }
               }
           }
           try {
               fileWriter.close();
           } catch ( IOException e ) {
               throw new InfoGatheringException( e);
           }
       }

       private void print( String ac, Sequence seq, Writer fileWriter ) throws InfoGatheringException {
           try {
               fileWriter.append( ">" + ac + "|description\n" + seq + "\n" );
           } catch ( IOException e ) {
              throw new InfoGatheringException( e);
           }
       }
    
       public static void main(String[] args) throws Exception{
            // 1. InfoGathering
           InfoGathering infoG = new InfoGathering( new SpokeExpansion() );
           File workDir = new File( "E:\\tmp", "ConfMain" );
           workDir.mkdir();
           try {
               infoG.retrieveHighConfidenceAndMediumConfidenceSetWithAnnotations( workDir );
           } catch ( InfoGatheringException e ) {
              throw new InfoGatheringException( e);
           } catch ( IOException e ) {
               throw new InfoGatheringException( e);
           }

//           BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
//           File inFile = new File( workDir, "hc_set.txt" );
//           //File inFile = new File("H:\\projects\\intact-current\\service\\commons\\confidence-score\\target\\test.txt");
//           Set<BinaryInteraction> setInts = new HashSet<BinaryInteraction>(bir.read( inFile ));
//           int lcNr = setInts.size();
//
//           File fastaFile = new File( "yeast fasta path in here" );
//           File lcFile = infoG.retrieveLowConfidenceSet( workDir, fastaFile, lcNr );
//           if ( lcFile != null ) {
//               infoG.retrieveLowConfidenceSetAnnotations( workDir, lcFile );
//           }
       }

}
