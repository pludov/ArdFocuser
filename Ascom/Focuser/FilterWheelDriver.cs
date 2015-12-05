//tabs=4
// --------------------------------------------------------------------------------
// ASCOM Focuser driver for Arduino FilterWheel
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
    [Guid("9117b7a1-d864-4081-952a-1b3be7249789")]
    [ClassInterface(ClassInterfaceType.None)]
    public class FilterWheel : BaseDriver, IFilterWheelV2
    {
        #region Constants
        /// <summary>
        /// ASCOM DeviceID (COM ProgID) for this driver.
        /// The DeviceID is used by ASCOM applications to load the driver at runtime.
        /// </summary>
        internal static string filterWheelDriverID = "ASCOM.Arduino.FilterWheel";     // tekkydave
        /// <summary>
        /// Driver description that displays in the ASCOM Chooser.
        /// </summary>
        private static string filterWheelDriverDescription = "ASCOM Filter Wheel Driver for Arduino Filter Wheel";     // tekkydave


        #endregion

        /// <summary>
        /// Initializes a new instance of the <see cref="ArduinoFocuser"/> class.
        /// Must be public for COM registration.
        /// </summary>
        public FilterWheel()
            : base("FilterWheel", filterWheelDriverID, filterWheelDriverDescription, 2)
        {
            
        }


        #region IFilterWheel Implementation


        public string[] Names
        {
            get
            {
                logMessage("Names Get", true.ToString());
                // FIXME
                return new string[] { "Vert", "Bleu", "Rouge" };;
            }
        }

        public int[] FocusOffsets
        {
            get
            {
                logMessage("FocusOffsets Get", true.ToString());
                int[] result = new int[Names.Length];
                for (int i = 0; i < result.Length; ++i)
                {
                    result[i] = 0;
                }
                return result;
            }
        }

        short p = 0;
        public short Position {
            
            get
            {
                logMessage("Position Get", true.ToString());
                // fixme
                return p; // focuser.getFilterWheelCurrentFilter();
            }
            set
            {
                logMessage("Position Set", value.ToString());
                // fixme
                p = value;
                // return focuser.setFilterWheelCurrentFilter(value);
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
                P.DeviceType = "FilterWheel";
                if (bRegister)
                {
                    P.Register(filterWheelDriverID, filterWheelDriverDescription);
                }
                else
                {
                    P.Unregister(filterWheelDriverID);
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
