//----------------------------------------------------------------------------//
//                                                                            //
//                             I m p l e m e n t                              //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.               //
//  This software is released under the terms of the GNU General Public       //
//  License. Please contact the author at herve.bitteur@laposte.net           //
//  to report bugs & suggestions.                                             //
//----------------------------------------------------------------------------//
//
package omr.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation <code>Implement</code> is used to flag a method which is (part of)
 * the implementation of an interface. It is foreseen to be used by a tool such
 * 'apt' in the future.
 */
@Documented
@Target(ElementType.METHOD)
public @interface Implement
{
    /** Interface whose method is implemented */
    Class value();
}
