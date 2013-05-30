using System;
using System.Runtime.InteropServices;

namespace scienceengineios
{

    // You should to create a new class to deal with wrapping each native library you need to call
    //  functions from, 
    // because you'll ultimately want to have a classname.functionname way of calling the library.
    
    public class TextToSpeech
    {
        IntPtr voice;
    // Dllimport function from System.Runtime.InteropServices
    // For this application, you import the DLL through GCC flags and call it via
    //  [Dllimport "__Internal")].  This is because
    // Monotouch statically compiles everything, so you're actually declaring the function whose body
    // is INSIDE the Native Library (after compilation)        
    
    // This first function is in libflite.a, the compiled C library you're linking up with.  
    // Research Cross Compiling to find out how to convert C and C++ projects to the iPhone Arm6
    // processor.
        
    // Any native library must be compiled into a .a extension for the Arm6 processor on the iPhone:
    // HOWEVER!!! NOTE:  When you use the iPhone simluator to debug this, you must use a library that
    // was compiled for the 1386 processor in order for it to work.
    // This is where I learned that the iPhone simulator is NOT an iPhone emulator.
        
        
       // This first DllImport deals with the first function called in libflite.a
  
        [DllImport ("__Internal")]
                
        //  The flite_init function of the library must be called before any other function in the
        //  library towork.
        //  This is to allow room to  for the flite devs build in future functionality, and right now
        //  does nothing more than allow the rest of the functions to be called after its activation.

        static extern void flite_init ();
        
        public void fliteInitFunc() {
           flite_init();
           voice = register_cmu_us_kal();
           flite_set_features(voice, 95f, 11f, 1f);
        }
        //  You must do a new DllImport for each function you link up inside your class to the C
        //  libraries.  This second link creates an IntPtr to the library where the "Computerized
        //  Voice Object" lives.

        [DllImport ("__Internal")]
        //  Esentially, for each one of these voice libraries you have, you'll have a different
        //  "person's" voice.
        //  This is how you can modularly change the voice sound, clarity, gender, age, etc, of the
        //  voice.
        //  The register_cmu_us_kal function is a function in the c library - any of these voices will
        //  have a register_cmu_country_name function will exist in any properly made voice library
        //  for flite. This is very fast compared to others - male.
        static extern IntPtr register_cmu_us_kal();
        
		[DllImport ("__Internal")]
		//  male
		static extern IntPtr register_cmu_us_kal16();

		[DllImport ("__Internal")]
		//  female
		static extern IntPtr register_cmu_us_slt();
        //  Dllimport for setting features
        [DllImport ("__Internal")]
        
        // set the pitch, stddev and speed
        static extern void flite_set_features(IntPtr voice, float pitch, float stddev, float speed);

        [DllImport ("__Internal")]
        //  Actual C Function in libflite.a that you're calling:
        //  float flite_text_to_speech(const char *text, cst_voice *voice, const char *outtype);
        
        //  The three arguments this function takes are: The string you're outputting to voice, 
        //  the voice library you've declared above for use, and the output type you want.
        //  There are three out-types, but I'm only certain that one works for the iPhone at this
        //  time.

        //  You can set the out-type to play directly to the device using the argument "play"
        // (Proven not working 'out of the box", 
        //  you can set it to write to the file system by providing a path string )This works, and is
        //  how I call it later to be replayed- just record the .wav file, and then play it using the
        //  Mono-touch API. 
        //  Or you you can set it to none.  Not sure why you'd need that, but you have the option.
        //  I have only tested this with output to the filesystem
        static extern void flite_text_to_speech (string text, IntPtr voice, string audioFilePath);
        
        [DllImport ("__Internal")]        
        //  Actual C Function in libflite.a that you're calling:
        //  float flite_text_to_str(const char *text, char *out);
        
        //  The three arguments this function takes are: The string you're outputting to voice, 
        //   the voice and the output byte array.
        static extern int flite_text_to_wave_str(string text, IntPtr voice, byte[] str);
        
        byte[] fbytes = new byte[1000000];
        // Only one text can be spoken at a time because of bytes array being a single one.
        public byte[] ConvertTextToWavStr (String text)
		{
			int n = flite_text_to_wave_str(text, voice, fbytes);
			Console.Out.WriteLine("Wav file bytes = " + n);
			return fbytes;
		}
		
        //  The function to call all the functionalties above to output a text string to speech wav in file:
        public void ConvertTextToWav(string text, string audioFilePath) {      
          //  Call Flite Text to Speech function     
          flite_text_to_speech (text, voice, audioFilePath);                                       
        }
        
    }
}