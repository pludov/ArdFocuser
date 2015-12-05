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
    public class BaseDriver
    {
        #region Constants

        internal static string traceStateProfileName = "Trace Level";
        internal static string traceStateDefault = "false";

        internal static string tcpPortProfileName = "Tcp Port";
        internal static string tcpPortDefault = "";


        #endregion

        #region Variables

        internal bool traceState;
        internal int tcpPort;
        internal string deviceType; // Focuser
        internal string driverId;
        internal string driverDescription;
        internal short driverInterfaceVersion;

        /// <summary>
        /// Private variable to hold an ASCOM Utilities object
        /// </summary>
        private Util utilities;

        /// <summary>
        /// Private variable to hold an ASCOM AstroUtilities object to provide the Range method
        /// </summary>
        private AstroUtils astroUtilities;

        /// <summary>
        /// tekkydave - ArduinoFocuser object to hold all custom code relating to Arduino device
        /// </summary>
        public ArduinoFocuser focuser;  // tekkydave

        #endregion

        /// <summary>
        /// Initializes a new instance of the <see cref="ArduinoFocuser"/> class.
        /// Must be public for COM registration.
        /// </summary>
        public BaseDriver(string deviceType, string driverId, string driverDescription, short driverInterfaceVersion)
        {
            this.deviceType = deviceType;
            this.driverId = driverId;
            this.driverDescription = driverDescription;
            this.driverInterfaceVersion = driverInterfaceVersion;
            ReadProfile(); // Read device configuration from the ASCOM Profile store

            utilities = new Util(); //Initialise util object
            astroUtilities = new AstroUtils(); // Initialise astro utilities object
            focuser = new ArduinoFocuser(this);
        }

        private TraceLogger tl = null;

        public void logMessage(string id, string message)
        {
            if (traceState)
            {
                try
                {
                    if (tl == null)
                    {
                        tl = new TraceLogger("c:\\trace\\Arduino\\" + deviceType + "_" + DateTime.Now.ToString("yyyyMMddHHmmss"), "Arduino" + deviceType);      // Trace Logger
                        tl.Enabled = true;
                    }
                    tl.LogMessage(id, message);
                }
                catch
                {
                }
            }
        }

        ~BaseDriver() // Destructor
        {
            if (tl != null)
            {
                tl.Enabled = false;
                tl.Dispose();
            }
            tl = null;
        }

        //
        // PUBLIC COM INTERFACE IFocuserV2 IMPLEMENTATION
        //

        #region Common properties and methods.

        /// <summary>
        /// Displays the Setup Dialog form.
        /// If the user clicks the OK button to dismiss the form, then
        /// the new settings are saved, otherwise the old values are reloaded.
        /// THIS IS THE ONLY PLACE WHERE SHOWING USER INTERFACE IS ALLOWED!
        /// </summary>
        public void SetupDialog()
        {
            // consider only showing the setup dialog if not connected
            // or call a different dialog if connected
            if (IsConnected)
                System.Windows.Forms.MessageBox.Show("Already connected, just press OK");

            using (SetupDialogForm F = new SetupDialogForm(this)
            {
                // saveParameters = saveParametersImpl;//new Func<int,bool>();
                saveParameters = saveParametersImpl
            })
            {
                var result = F.ShowDialog();
                if (result == System.Windows.Forms.DialogResult.OK)
                {
                    WriteProfile(); // Persist device configuration values to the ASCOM Profile store
                }
            }
        }

        void saveParametersImpl(int port, bool traceState)
        {
           this.tcpPort = port;
           this.traceState = traceState;

           using (ASCOM.Utilities.Profile p = new Utilities.Profile())
           {
               p.DeviceType = deviceType;
               p.WriteValue(this.driverId, Focuser.tcpPortProfileName, "" + port);
               p.WriteValue(this.driverId, Focuser.traceStateProfileName, "" + traceState);
           }
        }

        public ArrayList SupportedActions
        {
            get
            {
                logMessage("SupportedActions Get", "Returning empty arraylist");
                return new ArrayList();
            }
        }


        public string Action(string actionName, string actionParameters)
        {
            throw new ASCOM.ActionNotImplementedException("Action " + actionName + " is not implemented by this driver");
        }

        public void CommandBlind(string command, bool raw)
        {
            // or
            throw new ASCOM.MethodNotImplementedException("CommandBlind");
        }

        public bool CommandBool(string command, bool raw)
        {
            throw new ASCOM.MethodNotImplementedException("CommandBool");
        }

        public string CommandString(string command, bool raw)
        {
            CheckConnected("CommandString");
            // it's a good idea to put all the low level communication with the device here,
            // then all communication calls this function
            // you need something to ensure that only one command is in progress at a time

            // throw new ASCOM.MethodNotImplementedException("CommandString");  // tekkydave - Deleted

            return focuser.CommandString(command, raw);    // tekkydave - Call AAF2.CommandString
        }

        public void Dispose()
        {
            // Clean up the tracelogger and util objects
            if (tl != null)
            {
                tl.Enabled = false;
                tl.Dispose();
                tl = null;
            }
            utilities.Dispose();
            utilities = null;
            astroUtilities.Dispose();
            astroUtilities = null;
        }

        public bool Connected
        {
            get
            {
                logMessage("Connected Get", IsConnected.ToString());
                return IsConnected;
            }
            set
            {
                logMessage("Connected Set", value.ToString());
                if (value == IsConnected)
                    return;

                if (value)
                {
                    if (focuser.isConnected())   // tekkydave - return if already connected
                        return;

                    logMessage("Connected Set", "Connecting");
                    // TODO connect to the device
                    focuser.connect();         // tekkydave - Connect to device
                }
                else
                {
                    logMessage("Connected Set", "Disconnecting");
                    // TODO disconnect from the device
                    focuser.disconnect();      // tekkydave - Disconnect from device
                }
            }
        }

        public string Description
        {
            // TODO customise this device description
            get
            {
                logMessage("Description Get", driverDescription);
                return driverDescription;
            }
        }

        public string DriverInfo
        {
            get
            {
                Version version = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version;
                // TODO customise this driver description
                //string driverInfo = "Information about the driver itself. Version: " + String.Format(CultureInfo.InvariantCulture, "{0}.{1}", version.Major, version.Minor);
                string driverInfo = focuser.DriverInfo + " Version: " + String.Format(CultureInfo.InvariantCulture, "{0}.{1}", version.Major, version.Minor);   // tekkydave - replaced above line with my definition
                logMessage("DriverInfo Get", driverInfo);
                return driverInfo;
            }
        }

        public string DriverVersion
        {
            get
            {
                Version version = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version;
                string driverVersion = String.Format(CultureInfo.InvariantCulture, "{0}.{1}", version.Major, version.Minor);
                logMessage("DriverVersion Get", driverVersion);
                return driverVersion;
            }
        }

        public short InterfaceVersion
        {
            // set by the driver wizard
            get
            {
                logMessage("InterfaceVersion Get", "" + driverInterfaceVersion);
                return driverInterfaceVersion;
            }
        }

        public string Name
        {
            get
            {
                //string name = "Short driver name - please customise";
                string name = focuser.Name;    // tekkydave - replaced line above with call to AAF2
                logMessage("Name Get", name);
                return name;
            }
        }

        #endregion

        #region IFocuser Implementation

        #endregion

        #region Private properties and methods
        // here are some useful properties and methods that can be used as required
        // to help with driver development

        /// <summary>
        /// Returns true if there is a valid connection to the driver hardware
        /// </summary>
        private bool IsConnected
        {
            get
            {
                return focuser.isConnected();
            }
        }

        /// <summary>
        /// Use this function to throw an exception if we aren't connected to the hardware
        /// </summary>
        /// <param name="message"></param>
        private void CheckConnected(string message)
        {
            if (!IsConnected)
            {
                throw new ASCOM.NotConnectedException(message);
            }
        }

        /// <summary>
        /// Read the device configuration from the ASCOM Profile store
        /// </summary>
        internal void ReadProfile()
        {
            using (Profile driverProfile = new Profile())
            {
                driverProfile.DeviceType = this.deviceType;
                traceState = Convert.ToBoolean(driverProfile.GetValue(this.driverId, traceStateProfileName, string.Empty, traceStateDefault));
                tcpPort = -1;
                try
                {
                    tcpPort = int.Parse(driverProfile.GetValue(this.driverId, tcpPortProfileName, string.Empty, tcpPortDefault));
                }
                catch
                {
                }
            }
        }

        /// <summary>
        /// Write the device configuration to the  ASCOM  Profile store
        /// </summary>
        internal void WriteProfile()
        {
            using (Profile driverProfile = new Profile())
            {
                driverProfile.DeviceType = this.deviceType;
                driverProfile.WriteValue(this.driverId, traceStateProfileName, traceState.ToString());
                driverProfile.WriteValue(this.driverId, tcpPortProfileName, tcpPort.ToString());
            }
        }

        #endregion

    }
}
