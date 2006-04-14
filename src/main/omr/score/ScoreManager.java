//-----------------------------------------------------------------------//
//                                                                       //
//                        S c o r e M a n a g e r                        //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.score;

import omr.Main;
import omr.util.FileUtil;
import omr.util.Logger;
import omr.util.XmlMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import omr.sheet.Sheet;

/**
 * Class <code>ScoreManager</code> handles a collection of score instances.
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
public class ScoreManager
{
    //~ Static variables/initializers -------------------------------------

    private static final Logger logger = Logger.getLogger(ScoreManager.class);

    // Specific glyph XML mapper
    private static XmlMapper xmlMapper;

    // The single instance of this class
    private static ScoreManager INSTANCE;

    //~ Instance variables ------------------------------------------------

    // Instances of score
    private List<Score> instances = new ArrayList<Score>();

    //~ Constructors ------------------------------------------------------

    //--------------//
    // ScoreManager //
    //--------------//
    /**
     * Creates a Score Manager.
     */
    private ScoreManager ()
    {
    }

    //~ Methods -----------------------------------------------------------

    //--------------//
    // getXmlMapper //
    //--------------//
    private XmlMapper getXmlMapper()
    {
        if (xmlMapper == null) {
            xmlMapper = new XmlMapper(Score.class);
        }
        return xmlMapper;
    }

    //---------------//
    // checkInserted //
    //---------------//
    /**
     * Register score in score instances if not yet done
     */
    public void checkInserted (Score score)
    {
        if (!instances.contains(score)) {
            insertInstance(score);
        }
    }

    //-------//
    // close //
    //-------//
    /**
     * Close a score instance
     */
    public void close (Score score)
    {
        if (logger.isFineEnabled()) {
            logger.fine("close " + score);
        }

        // Remove from list of instance
        if (instances.contains(score)) {
            instances.remove(score);
        }
    }

    //----------//
    // closeAll //
    //----------//
    /**
     * Close all score instances
     */
    public void closeAll ()
    {
        if (logger.isFineEnabled()) {
            logger.fine("closeAll");
        }

        for (Iterator<Score> it = instances.iterator(); it.hasNext();) {
            Score score = it.next();
            it.remove(); // Done here to avoid concurrent modification
            score.close();
        }
    }

    //-------------//
    // deserialize //
    //-------------//
    /**
     * Deserialize the provided binary file to allocate the corresponding
     * Score hierarchy
     *
     * @param file the file that contains the Score data in binary format
     *
     * @return the allocated Score (and its related classes such as Page,
     *         etc), or null if load has failed.
     */
    public Score deserialize (File file)
    {
        logger.info("Deserializing score from " + file + " ...");

        try {
            long s0 = java.lang.System.currentTimeMillis();
            ObjectInputStream s = new ObjectInputStream
                (new FileInputStream(file));

            Score score = (Score) s.readObject();
            s.close();

            long s1 = java.lang.System.currentTimeMillis();
            logger.info("Score deserialized in " + (s1 - s0) + " ms");
            return score;
        } catch (Exception ex) {
            logger.warning("Could not deserialize score from " + file);
            logger.warning(ex.toString());
            return null;
        }
    }

    //---------------//
    // dumpAllScores //
    //---------------//
    /**
     * Dump all score instances
     */
    public void dumpAllScores ()
    {
        java.lang.System.out.println("\n");
        java.lang.System.out.println("* All Scores *");

        for (Score score : instances) {
            java.lang.System.out.println("------------------------------");

            if (score.dumpNode()) {
                score.dumpChildren(1);
            }
        }

        java.lang.System.out.println("------------------------------");
    }

    //-------------//
    // getInstance //
    //-------------//
    /**
     * Report the single instance of this class,
     *
     * @return the single instance
     */
    public static ScoreManager getInstance ()
    {
        if (INSTANCE == null) {
            INSTANCE = new ScoreManager();
        }
        return INSTANCE;
    }

    //-----------//
    // getScores //
    //-----------//
    /**
     * Get the collection of scores currently handled by OMR
     *
     * @return The collection
     */
    public List<Score> getScores ()
    {
        return instances;
    }

    //----------------//
    // insertInstance //
    //----------------//
    private void insertInstance (Score score)
    {
        if (logger.isFineEnabled()) {
            logger.fine("insertInstance " + score);
        }

        if (score.getRadix() != null) {
            // Remove duplicate if any
            for (Iterator<Score> it = instances.iterator(); it.hasNext();) {
                Score s = it.next();

                if (logger.isFineEnabled()) {
                    logger.fine("checking with " + s.getRadix());
                }

                if (s.getRadix().equals(score.getRadix())) {
                    if (logger.isFineEnabled()) {
                        logger.fine("Removing duplicate " + s);
                    }

                    it.remove();
                    s.close();

                    break;
                }
            }

            // Insert new score instances
            instances.add(score);
        }
    }

    //---------------//
    // linkAllScores //
    //---------------//
    /**
     * Make an attempt to link every score to proper sheet entity
     */
    public void linkAllScores ()
    {
        if (logger.isFineEnabled()) {
            logger.fine("linkAllScores");
        }

        for (Score score : instances) {
            score.linkWithSheet();
        }
    }

    //------//
    // load //
    //------//
    /**
     * Load a score from its file
     *
     * @param file the file from which score has to be retrieved. Depending
     *             on the precise extension of the file, the score will be
     *             unmarshalled (by an XML binder) or de-serialized (by
     *             plain Java).
     *
     * @return the score, or null if load has failed
     */
    public Score load (File file)
    {
        logger.info("Loading score from " + file.getPath());

        // Make file canonical
        try {
            file = new File(file.getCanonicalPath());
        } catch (IOException ex) {
            logger.warning("Cannot get canonical form of file " +
                           file.getPath(), ex);

            return null;
        }

        Score score = null;

        String ext = FileUtil.getExtension(file);
        if (ext.equals(ScoreFormat.XML.extension)) {
            try {
                // This may take a while, and even fail ...
                score = (Score) getXmlMapper().load(file);
            } catch (Exception ex) {
            }
        } else if (ext.equals(ScoreFormat.BINARY.extension)) {
            // This may take a while, and even fail ...
            score = deserialize(file);
        } else {
            logger.warning ("Unrecognized score extension on " + file);
        }

        if (score != null) {
            // Some adjustments
            score.setRadix(FileUtil.getNameSansExtension(file));

            // Fix the container relationships
            score.setChildrenContainer();

            // Insert in list of instances
            insertInstance(score);

            // Link with sheet side ?
            score.linkWithSheet();

            // Update UI wrt to the current sheet step
//            Sheet sheet = score.getSheet();
            /// TBD HB
//             if (sheet != null) {
//                 sheet.getInstanceStep(sheet.currentStep()).displayUI();
//             }
        }

        return score;
    }

    //--------//
    // remove //
    //--------//
    /**
     * Remove a given score from memory
     *
     * @param score the score instance to remove
     */
    void remove (Score score)
    {
        // Remove from list of instance
        if (instances.contains(score)) {
            instances.remove(score);
        }
    }

    //-----------//
    // serialize //
    //-----------//
    /**
     * Serialize the score to its binary file, and remember the actual file
     * used
     */
    public void serialize (Score score)
        throws Exception
    {
        // Make sure the destination directory exists
        File dir = new File(Main.getOutputFolder());
        if (!dir.exists()) {
            logger.info("Creating directory " + dir);
            dir.mkdirs();
        }

        File file = new File
            (dir,
             score.getRadix() + ScoreFormat.BINARY.extension);
        logger.info("Serializing score to " + file + " ...");

        long s0 = java.lang.System.currentTimeMillis();
        ObjectOutput s = new ObjectOutputStream
            (new FileOutputStream(file));

        s.writeObject(score);
        s.close();
        long s1 = java.lang.System.currentTimeMillis();
        logger.info("Score serialized in " + (s1 - s0) + " ms");
    }

    //--------------//
    // serializeAll //
    //--------------//
    /**
     * Serialize all score instances
     */
    public void serializeAll ()
    {
        if (logger.isFineEnabled()) {
            logger.fine("serializeAll");
        }

        for (Score score : instances) {
            try {
                score.serialize();
            } catch (Exception ex) {
                logger.warning("Could not serialize " + score);
            }
        }
    }

    //-------//
    // store //
    //-------//
    /**
     * Marshal a score to its XML file
     */
    public void store (Score score)
    {
        store(score, null);
    }

    //-------//
    // store //
    //-------//
    /**
     * Marshal a score to its XML file
     */
    public void store (Score score,
                       File  xmlFile)
    {
        // Where do we write the score xml file?
        if (xmlFile == null) {
            xmlFile = new File(Main.getOutputFolder(),
                               score.getRadix() +
                               ScoreFormat.XML.extension);
        }

        // Make sure the folder exists
        File folder = new File(xmlFile.getParent());
        if (!folder.exists()) {
            logger.info("Creating folder " + folder);
            folder.mkdirs();
        }

        try {
            // Store to disk
            getXmlMapper().store(score, xmlFile);
            logger.info("Score stored to " + xmlFile);
        } catch (Exception ignored) {
            // Exception already signaled to the user
        }
    }

    //----------//
    // storeAll //
    //----------//
    /**
     * Store all score instances
     */
    public void storeAll ()
    {
        if (logger.isFineEnabled()) {
            logger.fine("storeAll");
        }

        for (Score score : instances) {
            score.store();
        }
    }
}
