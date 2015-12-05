//tabs=4
// --------------------------------------------------------------------------------
// ASCOM Focuser driver for Arduino Focuser
//
// Description:	This driver was written to support the Arduino-Ascom Focuser.
//              The protocol used to communicate with the device is a custom protocol
//              which will only work with that device. The device is based around an
//              Arduino Nano and a stepper motor plus driver board.
//              The Driver is built on the ASCOM focuser driver template.
//              All code that communicates with the Arduino is contained in a separate
//              class called ArduinoFocuser. The minimum changes to Driver.cs have been made to aid future
//              upgrades/rewrites and make it easier to see what's going on :-)
//              All template code that has been modified is marked with a "// tekkydave" comment.
//
//              The complete source code for the Arduino sketch including communications protocol,
//              schematics and parts list are on the project's SourceForge site at:
//              http://sourceforge.net/projects/arduinoascomfocuser/
//
//
// Implements:	ASCOM Focuser interface version: <To be completed by driver developer>
// Author:		tekkydave
//
// Edit Log:
//
// Date			Who	        Vers	Description
// -----------	----------  -----	-------------------------------------------------------
// 01-06-2014	tekkydave	1.0.0	Initial edit, created from ASCOM driver template
// 09/08/2014   tekkydave   2.0.1   Initial position set to 1000
// 16/08/2014   tekkydave   2.0.2   Halt function implemented with H# command
//                                  New I# command to set an initial position
// 03/10/2014   tekkydave   2.2.0   Implemented Temperature Sensing C# command
// 07/10/2014   tekkydave   2.2.1   Amended to use stored position from focuser
//                                  Changed Initial Position field behaviour. Now only sets
//                                  focuser initial position if not blank.
// ----------------------------------------------------------------------------------------
//


// This is used to define code in the template that is specific to one class implementation
// unused code canbe deleted and this definition removed.
#define Focuser

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using System.Runtime.InteropServices;

using ASCOM;
using ASCOM.Astrometry;
using ASCOM.Astrometry.AstroUtils;
using ASCOM.Utilities;
using ASCOM.DeviceInterface;
using System.Globalization;
using System.Collections;

namespace ASCOM.Arduino
{
    //
    // Your driver's DeviceID is ASCOM.Arduino.Focuser
    //
    // The Guid attribute sets the CLSID for ASCOM.Arduino.Focuser
    // The ClassInterface/None addribute prevents an empty interface called
    // _Arduino from being created and used as the [default] interface
    //
    
    /// <summary>
    /// ASCOM Focuser Driver for ArduinoFocuser.
    /// </summary>
    [Guid("9117b7a1-d864-4081-952a-0a2ae7249789")]
    [ClassInterface(ClassInterfaceType.None)]
    public class Focuser : BaseDriver, IFocuserV2
    {
        #region Constants
        /// <summary>
        /// ASCOM DeviceID (COM ProgID) for this driver.
        /// The DeviceID is used by ASCOM applications to load the driver at runtime.
        /// </summary>
        internal static string focuserDriverID = "ASCOM.Arduino.Focuser";     // tekkydave
        /// <summary>
        /// Driver description that displays in the ASCOM Chooser.
        /// </summary>
        private static string focuserDriverDescription = "ASCOM Focuser Driver for Arduino Focuser";     // tekkydave


        #endregion

        /// <summary>
        /// Initializes a new instance of the <see cref="ArduinoFocuser"/> class.
        /// Must be public for COM registration.
        /// </summary>
        public Focuser() : base("Focuser", focuserDriverID, focuserDriverDescription, 2)
        {
            
        }

        //
        // PUBLIC COM INTERFACE IFocuserV2 IMPLEMENTATION
        //

        #region IFocuser Implementation

        public bool Absolute
        {
            get
            {
                logMessage("Absolute Get", true.ToString());
                return true; // This is an absolute focuser
            }
        }

        public void Halt()
        {
            logMessage("Halt", "Stopping Focuser movement.");
            focuser.halt();
        }

        public bool IsMoving
        {
            get
            {
                logMessage("IsMoving Get", false.ToString());
                // return false; // This focuser always moves instantaneously so no need for IsMoving ever to be True
                return focuser.isMoving();     // tekkydave - call AAF2.ismoving
            }
        }

        public bool Link
        {
            get
            {
                logMessage("Link Get", this.Connected.ToString());
                return this.Connected; // Direct function to the connected method, the Link method is just here for backwards compatibility
            }
            set
            {
                logMessage("Link Set", value.ToString());
                this.Connected = value; // Direct function to the connected method, the Link method is just here for backwards compatibility
            }
        }

        public int MaxIncrement
        {
            get
            {
                int focuserSteps = focuser.getMaxStep();
                logMessage("MaxIncrement Get", focuserSteps.ToString());
                return focuserSteps; // Maximum change in one move
            }
        }

        public int MaxStep
        {
            get
            {
                int focuserSteps = focuser.getMaxStep();
                logMessage("MaxStep Get", focuserSteps.ToString());
                return focuserSteps; // Maximum extent of the focuser, so position range is 0 to 10,000
            }
        }

        public void Move(int Position)
        {
            logMessage("Move", Position.ToString());

            // Stop focuserPosition being set to negative values
            if (Position < 0)
                Position = 0;

            focuser.setTargetPosition(Position);     // tekkydave - call AAF2.setPosition to set target position
        }

        public int Position
        {
            get
            {
                return focuser.getPosition(); // Return the focuser position
            }
        }

        public double StepSize
        {
            get
            {
                //logMessage("StepSize Get", "Not implemented");                      // tekkydave - replaced with call to AAF2.Stepsize
                //throw new ASCOM.PropertyNotImplementedException("StepSize", false);    // tekkydave - replaced with call to AAF2.Stepsize
                return 1.0;
            }
        }

        public bool TempComp
        {
            get
            {
                logMessage("TempComp Get", false.ToString());
                return false;
            }
            set
            {
                logMessage("TempComp Set", "Not implemented");
                throw new ASCOM.PropertyNotImplementedException("TempComp", false);
            }
        }

        public bool TempCompAvailable
        {
            get
            {
                logMessage("TempCompAvailable Get", false.ToString());
                return false; // Temperature compensation is not available in this driver
            }
        }

        public double Temperature
        {
            get
            {
                //logMessage("Temperature Get", "Not implemented");                      // tekkydave - replaced with call to AAF2.getTemperature
                //throw new ASCOM.PropertyNotImplementedException("Temperature", false);    // tekkydave - replaced with call to AAF2.getTemperature
                return focuser.getTemperature();
            }
        }

        #endregion

        #region Private properties and methods
        // here are some useful properties and methods that can be used as required
        // to help with driver development

        #region ASCOM Registration

        // Register or unregister driver for ASCOM. This is harmless if already
        // registered or unregistered. 
        //
        /// <summary>
        /// Register or unregister the driver with the ASCOM Platform.
        /// This is harmless if the driver is already registered/unregistered.
        /// </summary>
        /// <param name="bRegister">If <c>true</c>, registers the driver, otherwise unregisters it.</param>
        private static void RegUnregASCOM(bool bRegister)
        {
            using (var P = new ASCOM.Utilities.Profile())
            {
                P.DeviceType = "Focuser";
                if (bRegister)
                {
                    P.Register(focuserDriverID, focuserDriverDescription);
                }
                else
                {
                    P.Unregister(focuserDriverID);
                }
            }
        }

        /// <summary>
        /// This function registers the driver with the ASCOM Chooser and
        /// is called automatically whenever this class is registered for COM Interop.
        /// </summary>
        /// <param name="t">Type of the class being registered, not used.</param>
        /// <remarks>
        /// This method typically runs in two distinct situations:
        /// <list type="numbered">
        /// <item>
        /// In Visual Studio, when the project is successfully built.
        /// For this to work correctly, the option <c>Register for COM Interop</c>
        /// must be enabled in the project settings.
        /// </item>
        /// <item>During setup, when the installer registers the assembly for COM Interop.</item>
        /// </list>
        /// This technique should mean that it is never necessary to manually register a driver with ASCOM.
        /// </remarks>
        [ComRegisterFunction]
        public static void RegisterASCOM(Type t)
        {
            RegUnregASCOM(true);
        }

        /// <summary>
        /// This function unregisters the driver from the ASCOM Chooser and
        /// is called automatically whenever this class is unregistered from COM Interop.
        /// </summary>
        /// <param name="t">Type of the class being registered, not used.</param>
        /// <remarks>
        /// This method typically runs in two distinct situations:
        /// <list type="numbered">
        /// <item>
        /// In Visual Studio, when the project is cleaned or prior to rebuilding.
        /// For this to work correctly, the option <c>Register for COM Interop</c>
        /// must be enabled in the project settings.
        /// </item>
        /// <item>During uninstall, when the installer unregisters the assembly from COM Interop.</item>
        /// </list>
        /// This technique should mean that it is never necessary to manually unregister a driver from ASCOM.
        /// </remarks>
        [ComUnregisterFunction]
        public static void UnregisterASCOM(Type t)
        {
            RegUnregASCOM(false);
        }

        #endregion


        #endregion
    }
}
