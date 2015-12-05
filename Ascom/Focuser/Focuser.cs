using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ASCOM.Utilities;
using System.Net.Sockets;

namespace ASCOM.Arduino
{
    public class ArduinoFocuser // : ASCOM.DeviceInterface.IFocuserV2
    {

        //
        // Summary:
        //     Set True to connect to the device. Set False to disconnect from the device.
        //      You can also read the property to check whether it is connected.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implementedDo not use a NotConnectedException here, that exception
        //     is for use in other methods that require a connection in order to succeed.
        public bool Connected { 
            get { 
                return this.isConnected(); 
            } 
            set {
                if (value != this.isConnected()) {
                    if (value) {
                        this.connect("");
                    } else {
                        this.disconnect();
                    }
                }
            }
        }
        //
        // Summary:
        //     Returns a description of the device, such as manufacturer and modelnumber.
        //     Any ASCII characters may be used.
        //
        // Exceptions:
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented
        public string Description
        { 
            get {
                return "Arduino hub focuser";
            }
        }
        //
        // Summary:
        //     Descriptive and version information about this ASCOM driver.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented This string may contain line endings and may be hundreds
        //     to thousands of characters long.  It is intended to display detailed information
        //     on the ASCOM driver, including version and copyright data.  See the ASCOM.DeviceInterface.IFocuserV2.Description
        //     property for information on the device itself.  To get the driver version
        //     in a parseable string, use the ASCOM.DeviceInterface.IFocuserV2.DriverVersion
        //     property.
        public string DriverInfo
        { 
            get {
                return "Arduino focuser hub driver";
            }
        }

        //
        // Summary:
        //     A string containing only the major and minor version of the driver.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented This must be in the form "n.n".  It should not to be
        //     confused with the ASCOM.DeviceInterface.IFocuserV2.InterfaceVersion property,
        //     which is the version of this specification supported by the driver.
        public string DriverVersion { get { return driverInfo; } }
        //
        // Summary:
        //     The interface version number that this device supports. Should return 2 for
        //     this interface version.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented Clients can detect legacy V1 drivers by trying to read
        //     ths property.  If the driver raises an error, it is a V1 driver. V1 did not
        //     specify this property. A driver may also return a value of 1. In other words,
        //     a raised error or a return value of 1 indicates that the driver is a V1 driver.
        public short InterfaceVersion { get { return 2; } }

        //
        // Summary:
        //     True if the focuser is currently moving to a new position. False if the focuser
        //     is stationary.
        //
        // Exceptions:
        //   ASCOM.NotConnectedException:
        //     If the driver is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented
        public bool IsMoving { get { return this.isMoving(); } }

        //
        // Summary:
        //     State of the connection to the focuser.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must throw an exception if the call was not successful Must be implemented
        //     Set True to start the connection to the focuser; set False to terminate the
        //     connection. The current connection status can also be read back through this
        //     property. An exception will be raised if the link fails to change state for
        //     any reason.
        //     Note
        //     The FocuserV1 interface was the only interface to name its "Connect" method
        //     "Link" all others named their "Connect" method as "Connected". All interfaces
        //     including Focuser now have a ASCOM.DeviceInterface.IFocuserV2.Connected method
        //     and this is the recommended method to use to "Connect" to Focusers exposing
        //     the V2 and later interfaces.
        //     Do not use a NotConnectedException here, that exception is for use in other
        //     methods that require a connection in order to succeed.
        public bool Link { get { return Connected; } set { Connected = value; } }
        //
        // Summary:
        //     Maximum increment size allowed by the focuser; i.e. the maximum number of
        //     steps allowed in one move operation.
        //
        // Exceptions:
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented For most focusers this is the same as the ASCOM.DeviceInterface.IFocuserV2.MaxStep
        //     property. This is normally used to limit the Increment display in the host
        //     software.
        public int MaxIncrement { get { return MaxStep; } }
        //
        // Summary:
        //     Maximum step position permitted.
        //
        // Exceptions:
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented The focuser can step between 0 and ASCOM.DeviceInterface.IFocuserV2.MaxStep.
        //     If an attempt is made to move the focuser beyond these limits, it will automatically
        //     stop at the limit.
        public int MaxStep { get { return getMaxStep(); } }
        //
        // Summary:
        //     The short name of the driver, for display purposes
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented
        public string Name { get { return name; } }

        //
        // Summary:
        //     Current focuser position, in steps.
        //
        // Exceptions:
        //   ASCOM.PropertyNotImplementedException:
        //     If the property is not available for this device.
        //
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception Valid only for absolute positioning
        //     focusers (see the ASCOM.DeviceInterface.IFocuserV2.Absolute property).  A
        //     ASCOM.PropertyNotImplementedException exception must be thrown if this device
        //     is a relative positioning focuser rather than an absolute position focuser.
        public int Position { get { return this.getPosition(); } }

        //
        // Summary:
        //     Step size (microns) for the focuser.
        //
        // Exceptions:
        //   ASCOM.PropertyNotImplementedException:
        //     If the focuser does not intrinsically know what the step size is.
        //
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception Must throw an exception if the focuser
        //     does not intrinsically know what the step size is.
        public double StepSize { get { return stepsize; } }

        //
        // Summary:
        //     Returns the list of action names supported by this driver.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented This method must return an empty arraylist if no actions
        //     are supported. Please do not throw a ASCOM.PropertyNotImplementedException.
        //     This is an aid to client authors and testers who would otherwise have to
        //     repeatedly poll the driver to determine its capabilities. Returned action
        //     names may be in mixed case to enhance presentation but will be recognised
        //     case insensitively in the ASCOM.DeviceInterface.IFocuserV2.Action(System.String,System.String)
        //     method.
        //     An array list collection has been selected as the vehicle for action names
        //     in order to make it easier for clients to determine whether a particular
        //     action is supported. This is easily done through the Contains method. Since
        //     the collection is also ennumerable it is easy to use constructs such as For
        //     Each ... to operate on members without having to be concerned about hom many
        //     members are in the collection.
        //     Collections have been used in the Telescope specification for a number of
        //     years and are known to be compatible with COM. Within .NET the ArrayList
        //     is the correct implementation to use as the .NET Generic methods are not
        //     compatible with COM.
        public System.Collections.ArrayList SupportedActions { get { return new System.Collections.ArrayList(); } }

        //
        // Summary:
        //     The state of temperature compensation mode (if available), else always False.
        //
        // Exceptions:
        //   ASCOM.PropertyNotImplementedException:
        //     If ASCOM.DeviceInterface.IFocuserV2.TempCompAvailable is False and an attempt
        //     is made to set ASCOM.DeviceInterface.IFocuserV2.TempComp to true.
        //
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception If the ASCOM.DeviceInterface.IFocuserV2.TempCompAvailable
        //     property is True, then setting ASCOM.DeviceInterface.IFocuserV2.TempComp
        //     to True puts the focuser into temperature tracking mode. While in temperature
        //     tracking mode, ASCOM.DeviceInterface.IFocuserV2.Move(System.Int32) commands
        //     will be rejected by the focuser. Set to False to turn off temperature tracking.
        //     If temperature compensation is not available, this property must always return
        //     False.
        //     A ASCOM.PropertyNotImplementedException exception must be thrown if ASCOM.DeviceInterface.IFocuserV2.TempCompAvailable
        //     is False and an attempt is made to set ASCOM.DeviceInterface.IFocuserV2.TempComp
        //     to true.
        public bool TempComp { get { return false; } set { if (value) throw new ASCOM.PropertyNotImplementedException(); } }

        //
        // Summary:
        //     True if focuser has temperature compensation available.
        //
        // Exceptions:
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented Will be True only if the focuser's temperature compensation
        //     can be turned on and off via the ASCOM.DeviceInterface.IFocuserV2.TempComp
        //     property.
        public bool TempCompAvailable { get { return false; } }

        //
        // Summary:
        //     Current ambient temperature as measured by the focuser.
        //
        // Exceptions:
        //   ASCOM.PropertyNotImplementedException:
        //     If the property is not available for this device.
        //
        //   ASCOM.NotConnectedException:
        //     If the device is not connected and this information is only available when
        //     connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception Raises an exception if ambient temperature
        //     is not available. Commonly available on focusers with a built-in temperature
        //     compensation mode.
        public double Temperature { get { return getTemperature(); } }

        // Summary:
        //     Invokes the specified device-specific action.
        //
        // Parameters:
        //   ActionName:
        //     A well known name agreed by interested parties that represents the action
        //     to be carried out.
        //
        //   ActionParameters:
        //     List of required parameters or an System.String if none are required.
        //
        // Returns:
        //     A string response. The meaning of returned strings is set by the driver author.
        //
        // Exceptions:
        //   ASCOM.MethodNotImplementedException:
        //     Throws this exception if no actions are suported.
        //
        //   ASCOM.ActionNotImplementedException:
        //     It is intended that the SupportedActions method will inform clients of driver
        //     capabilities, but the driver must still throw an ASCOM.ActionNotImplemented
        //     exception if it is asked to perform an action that it does not support.
        //
        //   ASCOM.NotConnectedException:
        //     If the driver is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception This method is intended for use in
        //     all current and future device types and to avoid name clashes, management
        //     of action names is important from day 1. A two-part naming convention will
        //     be adopted - DeviceType:UniqueActionName where: DeviceType is the same value
        //     as would be used by ASCOM.Utilities.Chooser.DeviceType e.g. Telescope, Camera,
        //     Switch etc.  UniqueActionName is a single word, or multiple words joined
        //     by underscore characters, that sensibly describes the action to be performed.
        //     It is recommended that UniqueActionNames should be a maximum of 16 characters
        //     for legibility.  Should the same function and UniqueActionName be supported
        //     by more than one type of device, the reserved DeviceType of “General” will
        //     be used. Action names will be case insensitive, so FilterWheel:SelectWheel,
        //     filterwheel:selectwheel and FILTERWHEEL:SELECTWHEEL will all refer to the
        //     same action.
        //     The names of all supported actions must bre returned in the ASCOM.DeviceInterface.IFocuserV2.SupportedActions
        //     property.
        public string Action(string ActionName, string ActionParameters)
        {
            throw new ASCOM.MethodNotImplementedException();
        }
        //
        // Summary:
        //     Transmits an arbitrary string to the device and does not wait for a response.
        //      Optionally, protocol framing characters may be added to the string before
        //     transmission.
        //
        // Parameters:
        //   Command:
        //     The literal command string to be transmitted.
        //
        //   Raw:
        //     if set to true the string is transmitted 'as-is'.  If set to false then protocol
        //     framing characters may be added prior to transmission.
        //
        // Exceptions:
        //   ASCOM.MethodNotImplementedException:
        //     If the method is not implemented
        //
        //   ASCOM.NotConnectedException:
        //     If the driver is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception
        public void CommandBlind(string Command, bool Raw = false)
        {
            throw new ASCOM.MethodNotImplementedException();
        }

        //
        // Summary:
        //     Transmits an arbitrary string to the device and waits for a boolean response.
        //      Optionally, protocol framing characters may be added to the string before
        //     transmission.
        //
        // Parameters:
        //   Command:
        //     The literal command string to be transmitted.
        //
        //   Raw:
        //     if set to true the string is transmitted 'as-is'.  If set to false then protocol
        //     framing characters may be added prior to transmission.
        //
        // Returns:
        //     Returns the interpreted boolean response received from the device.
        //
        // Exceptions:
        //   ASCOM.MethodNotImplementedException:
        //     If the method is not implemented
        //
        //   ASCOM.NotConnectedException:
        //     If the driver is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception
        public bool CommandBool(string Command, bool Raw = false)
        {
            throw new ASCOM.MethodNotImplementedException();
        }

        //
        // Summary:
        //     Transmits an arbitrary string to the device and waits for a string response.
        //      Optionally, protocol framing characters may be added to the string before
        //     transmission.
        //
        // Parameters:
        //   Command:
        //     The literal command string to be transmitted.
        //
        //   Raw:
        //     if set to true the string is transmitted 'as-is'.  If set to false then protocol
        //     framing characters may be added prior to transmission.
        //
        // Returns:
        //     Returns the string response received from the device.
        //
        // Exceptions:
        //   ASCOM.MethodNotImplementedException:
        //     If the method is not implemented
        //
        //   ASCOM.NotConnectedException:
        //     If the driver is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exception
        public string CommandString(string Command, bool Raw = false)
        {
            throw new ASCOM.MethodNotImplementedException();
        }

        //
        // Summary:
        //     Dispose the late-bound interface, if needed. Will release it via COM if it
        //     is a COM object, else if native .NET will just dereference it for GC.
        public void Dispose()
        {
            Connected = false;
        }

        //
        // Summary:
        //     Moves the focuser by the specified amount or to the specified position depending
        //     on the value of the ASCOM.DeviceInterface.IFocuserV2.Absolute property.
        //
        // Parameters:
        //   Position:
        //     Step distance or absolute position, depending on the value of the ASCOM.DeviceInterface.IFocuserV2.Absolute
        //     property.
        //
        // Exceptions:
        //   ASCOM.InvalidOperationException:
        //     If a Move operation is requested when ASCOM.DeviceInterface.IFocuserV2.TempComp
        //     is True
        //
        //   ASCOM.NotConnectedException:
        //     If the device is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented If the ASCOM.DeviceInterface.IFocuserV2.Absolute property
        //     is True, then this is an absolute positioning focuser. The ASCOM.DeviceInterface.IFocuserV2.Move(System.Int32)
        //     command tells the focuser to move to an exact step position, and the Position
        //     parameter of the ASCOM.DeviceInterface.IFocuserV2.Move(System.Int32) method
        //     is an integer between 0 and ASCOM.DeviceInterface.IFocuserV2.MaxStep.
        //     If the ASCOM.DeviceInterface.IFocuserV2.Absolute property is False, then
        //     this is a relative positioning focuser. The ASCOM.DeviceInterface.IFocuserV2.Move(System.Int32)
        //     command tells the focuser to move in a relative direction, and the Position
        //     parameter of the ASCOM.DeviceInterface.IFocuserV2.Move(System.Int32) method
        //     (in this case, step distance) is an integer between minus ASCOM.DeviceInterface.IFocuserV2.MaxIncrement
        //     and plus ASCOM.DeviceInterface.IFocuserV2.MaxIncrement.
        public void Move(int Position)
        {
            setTargetPosition(Position);
        }

        //
        // Summary:
        //     Launches a configuration dialog box for the driver. The call will not return
        //     until the user clicks OK or cancel manually.
        //
        // Exceptions:
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Must be implemented
        public void SetupDialog()
        {
        }


        // Move to socket 
        // private static Serial serialPort;                                   // Serial Port
        private static System.Net.Sockets.TcpClient clientSocket = new System.Net.Sockets.TcpClient();
        private static double stepsize = 1;                                 // No of steps to move focusser motor for each value in Move() call
        private static string driverInfo = "Ascom-Arduino Focuser V2.";     // Driver Info String
        private static string name = "Arduino.Focuser";                                // Driver Short Name
        private Focuser driver;

        public ArduinoFocuser(Focuser driver)
        {
            this.driver = driver;
        }

        void logMessage(string id, string message)
        {
            this.driver.logMessage(id, message);
        }

        ~ArduinoFocuser() // Destructor
        {
        }

        public string internalCommandString(string command, bool raw)
        {
            if (!clientSocket.Connected) {
                throw new ASCOM.NotConnectedException();
            }
            logMessage("AAF2.CommandString", "------------------ Start -----------------------");
            logMessage("AAF2.CommandString", "Command = " + command);
            string s = null;
            logMessage("AAF2.CommandString", "Transmitting:" + command);
            NetworkStream serverStream = clientSocket.GetStream();
            byte[] outStream = System.Text.Encoding.GetEncoding(28591).GetBytes(command);
            serverStream.Write(outStream, 0, outStream.Length);
            serverStream.Flush();
            // Moved to socket: serialPort.Transmit(command);
            // Moved to socket: System.Threading.Thread.Sleep(250);
            logMessage("AAF2.CommandString", "Getting Return Message");

            // Moved to socket:  string result = serialPort.ReceiveTerminated("#");
            int b;
            string result = "";
            do
            {
                b = serverStream.ReadByte();
                if (b == -1)
                {
                    throw new Exception("disonnected");
                }
                if ((char)b == '#') {
                    break;
                }
                result += (char)b;
            } while(true);
            logMessage("AAF2.CommandString", "Return Message = " + result);

            if (result.StartsWith("ERR")) {
                throw new ASCOM.DriverException("Order failed");
            }

            logMessage("AAF2.CommandString", "------------------ Finish ----------------------");
            return result;
        }

        public void connect(string driverID)
        {
            // DMW connect to the device
            int tcpPort = Focuser.tcpPort;

            // try to connect to port
            try
            {
                clientSocket.ReceiveTimeout = 10000;
                clientSocket.SendTimeout = 10000;
                // FIXME : le port
                clientSocket.Connect("127.0.0.1", tcpPort);
                
                // Moved to socket: serialPort = new Serial();
                // Moved to socket: serialPort.PortName = portName;
                // Moved to socket: serialPort.Speed = SerialSpeed.ps9600;
                // Moved to socket: serialPort.StopBits = SerialStopBits.One;
                // Moved to socket: serialPort.ReceiveTimeout = 15;
                // Moved to socket: serialPort.Connected = true;
            }
            catch (Exception ex)
            {
                throw new ASCOM.NotConnectedException("Tcp Connection error on port " + tcpPort + " - is ArduinoFocuserUI connected ?", ex);
            }

            // Moved to socket: System.Threading.Thread.Sleep(2000);    // Wait 2s for connection to settle
        }

        public void disconnect()
        {
            // DMW disconnect from the device
            clientSocket.Close();
            // Moved to socket: serialPort.Connected = false;
            // Moved to socket: serialPort.Dispose();
        }

        public bool isConnected()
        {
            // Moved to socket: if (serialPort != null && serialPort.Connected)
            if (clientSocket != null && clientSocket.Connected)
                return true;
            else
                return false;
        }

        public void setInitialPosition(int Position)
        {
            string command = "I" + Position.ToString() + "#";
            logMessage("AAF2.setInitialPosition", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.setInitialPosition", "Received: " + r);
        }

        public void setTargetPosition(int Position)
        {
            string command = "T" + Position.ToString() + "#";
            logMessage("AAF2.setPosition", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.setPosition", "Received: " + r);
        }

        public int getPosition()
        {
            string command = "P" + "#";
            logMessage("AAF2.getPosition", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.getPosition", "Received: " + r);
            string[] w = r.Split(':');
            string p = w[0].Substring(1);
            logMessage("AAF2.getPosition", "Position = " + p);
            return Int32.Parse(p);
        }

        public int getMaxStep()
        {
            string command = "R" + "#";
            logMessage("AAF2.getMaxStep", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.getMaxStep", "Received: " + r);
            string[] w = r.Split(':');
            string p = w[0].Substring(1);
            logMessage("AAF2.getMaxStep", "Position = " + p);
            return Int32.Parse(p);
        }

        public double getTemperature()
        {
            string command = "C" + "#";
            logMessage("AAF2.getTemperature", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.getTemperature", "Received: " + r);
            string[] w = r.Split(':');
            string p = w[0].Substring(1);
            logMessage("AAF2.getTemperature", "Temperature = " + p);
            return Double.Parse(p)/100D;
        }

        internal bool isMoving()
        {
            bool result;
            string command = "M#";
            logMessage("AAF2.isMoving", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.isMoving", "Received: " + r);
            
            string[] w = r.Split(':');
            string p = w[0].Substring(1);
            
            if (p == "1")
            {
                logMessage("AAF2.isMoving", "Focuser is Moving");
                result = true;
            }
            else if (p == "0")
            {
                logMessage("AAF2.isMoving", "Focuser is Not Moving");
                result = false;
            }
            else
            {
                logMessage("AAF2.isMoving", "Unable to say");
                result = false;
            }

            return result;
        }

        //
        // Summary:
        //     Immediately stop any focuser motion due to a previous ASCOM.DeviceInterface.IFocuserV2.Move(System.Int32)
        //     method call.
        //
        // Exceptions:
        //   ASCOM.MethodNotImplementedException:
        //     Focuser does not support this method.
        //
        //   ASCOM.NotConnectedException:
        //     If the driver is not connected.
        //
        //   ASCOM.DriverException:
        //     Must throw an exception if the call was not successful
        //
        // Remarks:
        //     Can throw a not implemented exceptionSome focusers may not support this function,
        //     in which case an exception will be raised.
        //     Recommendation: Host software should call this method upon initialization
        //     and, if it fails, disable the Halt button in the user interface.
        public void halt()
        {
            string command = "H#";
            logMessage("AAF2.halt", "Sending: " + command);
            string r = internalCommandString(command, true);
            logMessage("AAF2.halt", "Received: " + r);
        }
    }
}
