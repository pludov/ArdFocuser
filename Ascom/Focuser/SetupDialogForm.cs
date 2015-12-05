using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;
using ASCOM.Utilities;
using ASCOM.Arduino;

namespace ASCOM.Arduino
{
    [ComVisible(false)]					// Form not registered for COM!
    public partial class SetupDialogForm : Form
    {
        public SetupDialogForm()
        {
            InitializeComponent();
            
            txtTcpPort.Text = Focuser.tcpPort != -1 ? "" + Focuser.tcpPort : "";
            chkTrace.Checked = Focuser.traceState;
            
        }

        public delegate void SaveParametersFunc(int port, bool trace);

        public SaveParametersFunc saveParameters;

        private void cmdOK_Click(object sender, EventArgs e) // OK button event handler
        {
            // Place any validation constraint checks here
            saveParameters.DynamicInvoke(String.IsNullOrWhiteSpace(txtTcpPort.Text) ? -1 : int.Parse(txtTcpPort.Text), chkTrace.Checked);

/*            Focuser.tcpPort = String.IsNullOrWhiteSpace(txtTcpPort.Text) ? -1 : int.Parse(txtTcpPort.Text);
            Focuser.traceState = chkTrace.Checked;


            using (ASCOM.Utilities.Profile p = new Utilities.Profile())
            {
                p.DeviceType = "Focuser";
                p.WriteValue(Focuser.driverID, Focuser.tcpPortProfileName, (string)txtTcpPort.Text);
                p.WriteValue(Focuser.driverID, Focuser.traceStateProfileName, (string)txtTcpPort.Text);
            }*/
        }

        private void cmdCancel_Click(object sender, EventArgs e) // Cancel button event handler
        {
            Close();
        }

        private void BrowseToAscom(object sender, EventArgs e) // Click on ASCOM logo event handler
        {
            try
            {
                System.Diagnostics.Process.Start("http://ascom-standards.org/");
            }
            catch (System.ComponentModel.Win32Exception noBrowser)
            {
                if (noBrowser.ErrorCode == -2147467259)
                    MessageBox.Show(noBrowser.Message);
            }
            catch (System.Exception other)
            {
                MessageBox.Show(other.Message);
            }
        }
    }
}