package msgfplus;

import java.io.File;
import java.net.URISyntaxException;

import edu.ucsd.msjava.msdbsearch.SuffixArrayForMSGFDB;
import edu.ucsd.msjava.msutil.Composition;
import edu.ucsd.msjava.params.ParamManager;
import edu.ucsd.msjava.ui.MSGFPlus;
import org.junit.Ignore;
import org.junit.Test;

import edu.ucsd.msjava.msdbsearch.CompactFastaSequence;
import edu.ucsd.msjava.msdbsearch.DBScanner;
import edu.ucsd.msjava.msgf.Tolerance;
import edu.ucsd.msjava.msutil.AminoAcid;
import edu.ucsd.msjava.msutil.AminoAcidSet;
import edu.ucsd.msjava.suffixarray.SuffixArray;
import edu.ucsd.msjava.suffixarray.SuffixArraySequence;

public class TestSA {

    @Test
    public void getAAProbabilities() throws URISyntaxException {
        File dbFile = new File(TestSA.class.getClassLoader().getResource("human-uniprot-contaminants.fasta").toURI());
        AminoAcidSet aaSet = AminoAcidSet.getStandardAminoAcidSetWithFixedCarbamidomethylatedCys();
        DBScanner.setAminoAcidProbabilities(dbFile.getPath(), aaSet);
        for(AminoAcid aa : aaSet)
        {
            System.out.println(aa.getResidue()+"\t"+aa.getProbability());
        }
    }
    
    @Test
    public void getNumCandidatePeptides() throws URISyntaxException {
        ParamManager paramManager = getParamManager();
        File dbFile = new File(TestSA.class.getClassLoader().getResource("human-uniprot-contaminants.fasta").toURI());
        SuffixArraySequence sequence = new SuffixArraySequence(dbFile.getPath());
        SuffixArray sa = new SuffixArray(sequence);
        String modFilePath = new File(TestSA.class.getClassLoader().getResource("Mods.txt").toURI()).getAbsolutePath();
        AminoAcidSet aaSet = AminoAcidSet.getAminoAcidSetFromModFile(modFilePath, paramManager);
        System.out.println("NumPeptides: " + sa.getNumCandidatePeptides(aaSet, 2364.981689453125f, new Tolerance(10, true)));
    }

    
    @Test
    @Ignore
    public void testRedundantProteins() throws URISyntaxException {
        File databaseFile = new File(TestSA.class.getClassLoader().getResource("ecoli-reversed.fasta").toURI());
        
        CompactFastaSequence fastaSequence = new CompactFastaSequence(databaseFile.getPath());
        fastaSequence.setDecoyProteinPrefix(MSGFPlus.DEFAULT_DECOY_PROTEIN_PREFIX);

        float ratioUniqueProteins = fastaSequence.getRatioUniqueProteins();
        if(ratioUniqueProteins < 0.5f)
        {
            fastaSequence.printTooManyDuplicateSequencesMessage(databaseFile.getName(), "MS-GF+", ratioUniqueProteins);
            System.exit(-1);
        }
        
        float fractionDecoyProteins = fastaSequence.getFractionDecoyProteins();
        if(fractionDecoyProteins < 0.4f || fractionDecoyProteins > 0.6f)
        {
            System.err.println("Error while reading: " + databaseFile.getName() + " (fraction of decoy proteins: " + fractionDecoyProteins + ")");
            System.err.println("Delete " + databaseFile.getName() + " and run MS-GF+ (or BuildSA) again.");
            System.err.println("Decoy protein names should start with " + fastaSequence.getDecoyProteinPrefix());
            System.exit(-1);
        }
        
    }

    @Test
    public void testTSA() throws Exception {
        File dbFile = new File(TestSA.class.getClassLoader().getResource("human-uniprot-contaminants.fasta").toURI());
        SuffixArraySequence sequence = new SuffixArraySequence(dbFile.getPath());

        long time;
        System.out.println("SuffixArrayForMSGFDB");
        time = System.currentTimeMillis();
        SuffixArrayForMSGFDB sa2 = new SuffixArrayForMSGFDB(sequence);
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        int numCandidates = sa2.getNumCandidatePeptides(AminoAcidSet.getStandardAminoAcidSetWithFixedCarbamidomethylatedCys(), (383.8754f - (float) Composition.ChargeCarrierMass()) * 3 - (float) Composition.H2O, new Tolerance(2.5f, false));
        System.out.println("NumCandidatePeptides: " + numCandidates);
        int length10 = sa2.getNumDistinctPeptides(10);
        System.out.println("NumUnique10: " + length10);
    }

    private ParamManager getParamManager() {
        return new ParamManager("MS-GF+", MSGFPlus.VERSION, MSGFPlus.RELEASE_DATE, "java -Xmx3500M -jar MSGFPlus.jar");
    }

}
