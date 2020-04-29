#include "Linked_Map.h"

Linked_Map::Linked_Map(void)
{
	mapFileOutput = "DefaultMap.txt";
	mapFileOutputPassage = "DefaultMapPassage.txt";
	mapFileOutputInteractions = "DefaultMapInteractions.txt";

	for (int j = 0; j < mapSize; j++)
	{
		for (int k = 0; k < mapSize; k++)
		{
			nodeArray[j][k] = new node;
			nodeArray[j][k]->tile = GRASS;
			nodeArray[j][k]->canEnter = YES;
			nodeArray[j][k]->canUp = YES;
			nodeArray[j][k]->canDown = YES;
			nodeArray[j][k]->canRight = YES;
			nodeArray[j][k]->canLeft = YES;

			nodeArray[j][k]->canInteract = NO;
		}
	}

	for (int j = 0; j < mapSize; j++)
	{
		for (int k = 0; k < mapSize; k++)
		{
			if (j == 0)
			{
				nodeArray[j][k]->nodeUp = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeUp = nodeArray[j - 1][k];
			}

			if (j == mapSize - 1)
			{
				nodeArray[j][k]->nodeDown = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeDown = nodeArray[j + 1][k];
			}

			if (k == 0)
			{
				nodeArray[j][k]->nodeLeft = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeLeft = nodeArray[j][k - 1];
			}

			if (k == mapSize - 1)
			{
				nodeArray[j][k]->nodeRight = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeRight = nodeArray[j][k + 1];
			}
		}
	}

	currentLocation = nodeArray[mapSize / 2][mapSize / 2];
}

Linked_Map::Linked_Map(string finImage, string finPassage, string finInteractions)
{
	ifstream fin1;
	ifstream fin2;
	ifstream fin3;
	int finInput1;
	int finInput3;
	string finInput2;

	mapFileOutput = finImage;
	mapFileOutputPassage = finPassage;
	mapFileOutputInteractions = finInteractions;

	fin1.open(finImage);
	fin2.open(finPassage);
	fin3.open(finInteractions);

	for (int j = 0; j < mapSize; j++)
	{
		for (int k = 0; k < mapSize; k++)
		{
			nodeArray[j][k] = new node;

			fin1 >> finInput1;

			if (finInput1 == 0)
			{
				nodeArray[j][k]->tile = GRASS;
			}

			else if (finInput1 == 1)
			{
				nodeArray[j][k]->tile = PATH;
			}

			else if (finInput1 == 2)
			{
				nodeArray[j][k]->tile = WATER;
			}

			else if (finInput1 == 3)
			{
				nodeArray[j][k]->tile = LAVA;
			}
			

			fin2 >> finInput2;

			if (finInput2.at(0) == '0')
			{
				nodeArray[j][k]->canEnter = YES;
			}

			else
			{
				nodeArray[j][k]->canEnter = NO;
			}

			if (finInput2.at(1) == '0')
			{
				nodeArray[j][k]->canUp = YES;
			}

			else
			{
				nodeArray[j][k]->canUp = NO;
			}

			if (finInput2.at(2) == '0')
			{
				nodeArray[j][k]->canDown = YES;
			}

			else
			{
				nodeArray[j][k]->canDown = NO;
			}

			if (finInput2.at(3) == '0')
			{
				nodeArray[j][k]->canLeft = YES;
			}

			else
			{
				nodeArray[j][k]->canLeft = NO;
			}

			if (finInput2.at(4) == '0')
			{
				nodeArray[j][k]->canRight = YES;
			}

			else
			{
				nodeArray[j][k]->canRight = NO;
			}
			
			fin3 >> finInput3;

			if (finInput3 == 0)
			{
				nodeArray[j][k]->canInteract = NO;
			}

			else if (finInput3 == 1)
			{
				nodeArray[j][k]->canInteract = YES;
			}

		}
	}

	for (int j = 0; j < mapSize; j++)
	{
		for (int k = 0; k < mapSize; k++)
		{
			if (j == 0)
			{
				nodeArray[j][k]->nodeUp = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeUp = nodeArray[j - 1][k];
			}

			if (j == mapSize - 1)
			{
				nodeArray[j][k]->nodeDown = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeDown = nodeArray[j + 1][k];
			}

			if (k == 0)
			{
				nodeArray[j][k]->nodeLeft = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeLeft = nodeArray[j][k - 1];
			}

			if (k == mapSize - 1)
			{
				nodeArray[j][k]->nodeRight = NULL;
			}

			else
			{
				nodeArray[j][k]->nodeRight = nodeArray[j][k + 1];
			}
		}
	}

	currentLocation = nodeArray[mapSize / 2][mapSize / 2];

	fin1.close();
	fin2.close();
	fin3.close();
}

Linked_Map::~Linked_Map(void)
{
	ofstream foutMap;
	ofstream foutPassage;
	ofstream foutInteractions;

	foutMap.open(mapFileOutput);
	foutPassage.open(mapFileOutputPassage);
	foutInteractions.open(mapFileOutputInteractions);

	for (int k = 0; k < mapSize; k++)
	{
		for (int j = 0; j < mapSize; j++)
		{
			foutMap << nodeArray[k][j]->tile << ' ';
		}

		foutMap << endl;
	}

	string tempString = "00000";

	for (int k = 0; k < mapSize; k++)
	{
		for (int j = 0; j < mapSize; j++)
		{
			tempString = "00000";

			if (nodeArray[k][j]->canEnter == NO)
			{
				tempString.at(0) = '1'; 
			}

			if (nodeArray[k][j]->canUp == NO)
			{
				tempString.at(1) = '1';
			}

			if (nodeArray[k][j]->canDown == NO)
			{
				tempString.at(2) = '1';
			}

			if (nodeArray[k][j]->canLeft == NO)
			{
				tempString.at(3) = '1';
			}

			if (nodeArray[k][j]->canRight == NO)
			{
				tempString.at(4) = '1';
			}

			foutPassage << tempString << ' ';
		}

		foutPassage << endl;
	}

	for (int k = 0; k < mapSize; k++)
	{
		for (int j = 0; j < mapSize; j++)
		{
			if (nodeArray[k][j]->canInteract == YES)
			{
				foutInteractions << 1 << ' ';
			}

			else if (nodeArray[k][j]->canInteract == NO)
			{
				foutInteractions << 0 << ' ';
			}
		}

		foutInteractions << endl;
	}

	foutMap.close();
	foutPassage.close();
	foutInteractions.close();

	for (int j = 0; j < mapSize; j++)
	{
		for (int k = 0; k < mapSize; k++)
		{
			delete nodeArray[j][k];
		}
	}
}

void Linked_Map::updateCurrentLocation(Direction direction)
{
	if (direction == UP)
	{
		if (currentLocation->nodeUp != NULL)
		{
			currentLocation = currentLocation->nodeUp;
		}
	}

	else if (direction == DOWN)
	{
		if (currentLocation->nodeDown != NULL)
		{
			currentLocation = currentLocation->nodeDown;
		}
	}

	else if (direction == LEFT)
	{
		if (currentLocation->nodeLeft != NULL)
		{
			currentLocation = currentLocation->nodeLeft;
		}
	}

	else if (direction == RIGHT)
	{
		if (currentLocation->nodeRight != NULL)
		{
			currentLocation = currentLocation->nodeRight;
		}
	}
}


//----------------------------------------Not Yet Implemented--------------------------------------
void Linked_Map::moveMapLeft(ifstream fin)
{

}

void Linked_Map::moveMapRight(ifstream fin)
{

}

void Linked_Map::moveMapUp(ifstream fin)
{

}

void Linked_Map::moveMapDown(ifstream fin)
{

}
//-------------------------------------------------------------------------------------------------