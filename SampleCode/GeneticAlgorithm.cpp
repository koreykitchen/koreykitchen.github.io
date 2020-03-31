#include "Genetics.h"

Genetics::Genetics(void)
{
	generationNumber = 0;

	InitializePopulation();
}

//Need to generate numbers based on rules, not do completely random
//	EX: 60% for 1, 20% for 2, 10% for 3
//	EX: Percent chance based on surrounding tiles
void Genetics::InitializePopulation(void)
{
	string buildString;

	srand((int)time(NULL));

	for (int k = 0; k < POPULATION_SIZE; k++)
	{
		buildString = "";

		for (int j = 0; j < TOTAL_TILES; j++)
		{
			buildString.append(to_string((rand() % 3) + 1));
		}

		population[k].testString = buildString;
	}

	CalculatePopulationFitnesses();

	CopyPopulationToGenePool();
}

void Genetics::Evolve(void)
{
	generationNumber++;

	//CrossOver();

	Mutate();
}

void Genetics::CrossOver(void)
{
	string buildString;

	for (int k = POPULATION_SIZE; k < GENE_POOL_SIZE; k += 2)
	{
		buildString = "";

		buildString.append(genePool[k - POPULATION_SIZE].testString.substr(0, TOTAL_TILES / 2));
		buildString.append(genePool[k - POPULATION_SIZE + 1].testString.substr(TOTAL_TILES / 2, TOTAL_TILES - (TOTAL_TILES / 2)));

		genePool[k].testString = buildString;

		buildString = "";

		buildString.append(genePool[k - POPULATION_SIZE + 1].testString.substr(0, TOTAL_TILES / 2));
		buildString.append(genePool[k - POPULATION_SIZE].testString.substr(TOTAL_TILES / 2, TOTAL_TILES - (TOTAL_TILES / 2)));	

		genePool[k + 1].testString = buildString;
	}

	CalculateGenePoolFitnesses();

	CopyGenePoolToPopulation();
}

void Genetics::Mutate(void)
{
	string buildString;
	char randChar;

	int mostFailedIndex;
	int mostFailedNumber;
	int testingNumber;

	for (int k = POPULATION_SIZE; k < GENE_POOL_SIZE; k++)
	{
		mostFailedIndex = -1;
		mostFailedNumber = 0;

		buildString = genePool[k-POPULATION_SIZE].testString;

		randChar = to_string((rand() % 3) + 1).at(0);

		for (int o = 0; o < TOTAL_TILES; o++)
		{

			testingNumber = 4 - (tileIsConnected(genePool[k-POPULATION_SIZE].testString, o));

			if (testingNumber > mostFailedNumber)
			{
				mostFailedIndex = o;
				mostFailedNumber = testingNumber;
			}

		}

		buildString.at(mostFailedIndex) = randChar;

		genePool[k].testString = buildString;
	}

	CalculateGenePoolFitnesses();

	CopyGenePoolToPopulation();
}

void Genetics::CalculatePopulationFitnesses(void)
{
	for (int k = 0; k < POPULATION_SIZE; k++)
	{
		population[k].fitness = 0;

		//population[k].fitness += findRectangle(population[k].testString, 3, 3, Tile::GRASS);
		//population[k].fitness += findRectangle(population[k].testString, 3, 2, Tile::WATER);
		//population[k].fitness += findRectangle(population[k].testString, 2, 3, Tile::PATH);
		
		population[k].fitness += isConnected(population[k].testString);
	}

	qsort(population, POPULATION_SIZE, sizeof(Case), compareCases);
}

void Genetics::CalculateGenePoolFitnesses(void)
{
	for (int k = 0; k < GENE_POOL_SIZE; k++)
	{
		genePool[k].fitness = 0;

		//genePool[k].fitness += findRectangle(genePool[k].testString, 3, 3, Tile::GRASS);
		//genePool[k].fitness += findRectangle(genePool[k].testString, 3, 2, Tile::WATER);
		//genePool[k].fitness += findRectangle(genePool[k].testString, 2, 3, Tile::PATH);

		genePool[k].fitness += isConnected(genePool[k].testString);
	}

	qsort(genePool, GENE_POOL_SIZE, sizeof(Case), compareCases);
}

bool Genetics::FoundSuccess(void)
{
	if (population[0].fitness == 0)
	{
		return true;
	}

	return false;
}

void Genetics::CopyPopulationToGenePool(void)
{
	for (int k = 0; k < POPULATION_SIZE; k++)
	{
		genePool[k] = population[k];
	}
}

void Genetics::CopyGenePoolToPopulation(void)
{
	for (int k = 0; k < POPULATION_SIZE; k++)
	{
		population[k] = genePool[k];
	}
}

int compareCases(const void* lhs, const void* rhs)
{
	if ((*(Case*)lhs).fitness < (*(Case*)rhs).fitness)
	{
		return -1;
	}

	else if ((*(Case*)lhs).fitness == (*(Case*)rhs).fitness)
	{
		return 0;
	}

	else// if ((*(Case*)lhs).fitness > (*(Case*)rhs).fitness)
	{
		return 1;
	}
}