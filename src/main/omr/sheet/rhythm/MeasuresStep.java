//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                    M e a s u r e s S t e p                                     //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Hervé Bitteur and others 2000-2016. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.sheet.rhythm;

import omr.score.Page;

import omr.sheet.Sheet;
import omr.sheet.SystemInfo;

import omr.step.AbstractSystemStep;
import omr.step.StepException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@code MeasuresStep} allocates the measures from the relevant bar lines.
 *
 * @author Hervé Bitteur
 */
public class MeasuresStep
        extends AbstractSystemStep<Void>
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(MeasuresStep.class);

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new {@code MeasuresStep} object.
     */
    public MeasuresStep ()
    {
    }

    //~ Methods ------------------------------------------------------------------------------------
    //----------//
    // doSystem //
    //----------//
    @Override
    public void doSystem (SystemInfo system,
                          Void context)
            throws StepException
    {
        new MeasuresBuilder(system).buildMeasures();
    }

    //----------//
    // doEpilog //
    //----------//
    @Override
    protected void doEpilog (Sheet sheet,
                             Void context)
            throws StepException
    {
        // Assign basic measure ids
        for (Page page : sheet.getPages()) {
            page.numberMeasures();
            page.dumpMeasureCounts();
        }
    }
}