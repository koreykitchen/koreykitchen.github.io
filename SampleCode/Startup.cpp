#include <windows.h>
#include <Commdlg.h>
#include <iostream>
#include <limits>
#include <string>

using namespace std;

OPENFILENAME ofn ;

char szFile[100] ;


int WINAPI WinMain( HINSTANCE hInstance , HINSTANCE hPrevInstance , LPSTR lpCmdLine , int nCmdShow )
{
    bool correctInput;

    bool stillOpening = true;

    string selection;

    while(stillOpening)
    {
        correctInput = false;

        selection = "";

        //Get User Input
        while(!correctInput)
        {
            cout << "Select An Option:\n\n";
            cout << "1.) Start Windows\n";
            cout << "2.) Select An App To Open\n";
            cout << "3.) Shutdown\n\n";
            cout << "Your Selection: ";

            cin >> selection;

            if(!cin || (selection.length() != 1))
            {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n'); 

                cout << "\nPlease Enter A Valid Option...\n\n"; 

                system("pause");

                system("cls");
            }

            else if((selection.at(0) == '1') || (selection.at(0) == '2') || (selection.at(0) == '3'))
            {
                correctInput = true;
            }

            else
            {
                cout << "\nPlease Enter A Valid Option...\n\n"; 

                system("pause");

                system("cls");
            }
            
        }

        //Selected 1: Start Windows
        if(selection.at(0) == '1')
        {
            cout << "\nStarting Windows...\n\n";

            stillOpening = false;

            system("start C:\\Windows\\explorer.exe");

            system("start C:\\Windows\\explorer.exe");
        }
        
        //Selected 2: Open An Application
        else if(selection.at(0) == '2')
        {
            ZeroMemory(&ofn, sizeof(ofn));
            ofn.lStructSize = sizeof(ofn);
            ofn.hwndOwner = NULL;
            ofn.lpstrFile = szFile;
            ofn.lpstrFile[0] = '\0';
            ofn.nMaxFile = sizeof(szFile);
            ofn.lpstrFilter = "All\0*.*\0Text\0*.TXT\0";
            ofn.nFilterIndex = 1;
            ofn.lpstrFileTitle = NULL;
            ofn.nMaxFileTitle = 0;
            ofn.lpstrInitialDir = NULL ;
            ofn.Flags = OFN_PATHMUSTEXIST|OFN_FILEMUSTEXIST;

            GetOpenFileName(&ofn);

            string command = "start ";

            command.append(ofn.lpstrFile);

            if(command.length() == 6)
            {
                cout << "\nFailed To Select An Application...\n\n";

                system("pause");

                system("cls");
            }

            else
            {
                cout << "\nOpening The Application...\n\n";
                
                system(command.c_str());

                system("pause");

                system("cls");
            }
        }

        //Selected 3: Shutdown
        else if(selection.at(0) == '3')
        {
            cout << "\nShutting Down...\n\n";

            stillOpening = false;
            
            system("shutdown /s");
        }
    }

    system("pause");
	
    return 0;
}

//g++ Startup.cpp -lcomdlg32 -o Startup