using System;
using System.IO;

namespace ArduinoPhotoTest
{
    class Program
    {
        [STAThreadAttribute]
        static void Main(string[] args)
        {
            StreamReader file = new StreamReader(@"C:\Users\Bak\Documents\P5\Report\Design\PhotoSensor Tests\ArduNormalt+2,2k.txt");
            string line;
            int a0Min = 99999;
            int a1Min = 99999;
            int a0Avg = 0;
            int a1Avg = 0;
            int a0Max = 0;
            int a1Max = 0;
            int counter = 0;
            while ((line = file.ReadLine()) != null)
            {
                int a0 = Convert.ToInt32(line.Split(',')[0]);
                int a1 = Convert.ToInt32(line.Split(',')[1]);
                a0Avg += a0;
                a1Avg += a1;
                if (a0Min > a0) a0Min = a0;
                else if (a0Max < a0) a0Max = a0;
                if (a1Min > a1) a1Min = a1;
                else if (a1Max < a1) a1Max = a1;
                counter++;
            }
            a0Avg /= counter;
            a1Avg /= counter;
            double a0Diff = ((double)a0Max / (double)a0Min) * 100d - 100;
            double a1Diff = ((double)a1Max / (double)a1Min) * 100d - 100;
            string text = string.Format("a0Avg = {0}\na0Min = {1}\na0Max = {2}\na0Diff = {6}%\na1Avg = {3}\na1Min = {4}\na1Max = {5}\na1Diff = {7}%", 
                a0Avg, a0Min, a0Max, a1Avg, a1Min, a1Max, a0Diff, a1Diff);
            Console.WriteLine(text);
            System.Windows.Forms.Clipboard.SetText(text);
            Console.Read();
        }
    }
}
