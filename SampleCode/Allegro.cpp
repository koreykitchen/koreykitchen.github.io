//--------------------------------------------TODO-------------------------------------------------
//fix timer and fps stuff... only draw every 60 ticks, and only if map changed
//
//Select input file name.. select output file name
//
//save map offset (location).. input offset on load.. input map size on load..
//
//for canEnters editing: top quarter, left quarter, etc...
//
//array of Linked maps that surround the current linked map... drawing on the 4 corners..
//
//if cant enter, have an interactable? field in nodes (green circle similar to red x)
//
//simulate "smooth" movement (animations)..
//
//add music... better graphics..
//
//clean up code
//
//Put all map data into one file / "Encrypt" map files to save on memory ---> when to decrypt?
//
//Specific interactable messages.... lookup table... make node have interactable message string/value for table
//
//Load in house/tree/bush/tree2/arena/sign
//-------------------------------------------------------------------------------------------------

#include "Allegro.h"

int allegro_main(Linked_Map* map)
{
	//-----------------------------------Allegro Display Stuff-------------------------------------
	ALLEGRO_DISPLAY *display = NULL;
	
	al_init();

	int tileSize = 128;
	int displayWidthInTiles = 7;
	int displayHeightInTiles = 5;

	int displayWidth = displayWidthInTiles*tileSize;
	int displayHeight = displayHeightInTiles*tileSize;
	

	display = al_create_display(displayWidth, displayHeight);
	//---------------------------------------------------------------------------------------------

	//---------------------------------Allegro Bitmap Stuff----------------------------------------
	ALLEGRO_BITMAP *grassBitmap = NULL;
	ALLEGRO_BITMAP *pathBitmap = NULL;
	ALLEGRO_BITMAP *waterBitmap = NULL;
	ALLEGRO_BITMAP *blankBitmap = NULL;
	ALLEGRO_BITMAP *lavaBitmap = NULL;
	ALLEGRO_BITMAP *characterDownBitmap = NULL;
	ALLEGRO_BITMAP *characterUpBitmap = NULL;
	ALLEGRO_BITMAP *characterLeftBitmap = NULL;
	ALLEGRO_BITMAP *characterRightBitmap = NULL;
	ALLEGRO_BITMAP *redX = NULL;
	ALLEGRO_BITMAP *greenCircle = NULL; 
	al_init_image_addon();
	grassBitmap = al_load_bitmap("Grass.png");
	pathBitmap = al_load_bitmap("CaveTile.png");
	waterBitmap = al_load_bitmap("Water.png");
	blankBitmap = al_load_bitmap("Blank.png");
	lavaBitmap = al_load_bitmap("Lava.png");
	characterDownBitmap = al_load_bitmap("GuyDown.png");
	characterUpBitmap = al_load_bitmap("GuyUp.png");
	characterLeftBitmap = al_load_bitmap("GuyLeft.png");
	characterRightBitmap = al_load_bitmap("GuyRight.png");
	redX = al_load_bitmap("RedX.png"); 
	greenCircle = al_load_bitmap("greenCircle.png"); 

	al_convert_mask_to_alpha(characterDownBitmap, al_map_rgb(255, 255, 255)); 
	al_convert_mask_to_alpha(characterUpBitmap, al_map_rgb(255, 255, 255));
	al_convert_mask_to_alpha(characterLeftBitmap, al_map_rgb(255, 255, 255));
	al_convert_mask_to_alpha(characterRightBitmap, al_map_rgb(255, 255, 255));
	al_convert_mask_to_alpha(redX, al_map_rgb(255, 255, 255)); 
	al_convert_mask_to_alpha(greenCircle, al_map_rgb(255, 255, 255));

	int tileWidth = al_get_bitmap_width(grassBitmap);
	int tileHeight = al_get_bitmap_height(grassBitmap);
	//---------------------------------------------------------------------------------------------

	//---------------------------------Allegro Sound Stuff-----------------------------------------
	al_install_audio();
	al_init_acodec_addon();
	al_reserve_samples(2);

	ALLEGRO_SAMPLE *bumpSound = NULL;
	ALLEGRO_SAMPLE *mapMusic = NULL;

	bumpSound = al_load_sample("BumpSound.wav");
	mapMusic = al_load_sample("MapMusic3.wav");
	//---------------------------------------------------------------------------------------------
	
	//----------------------------------Allegro Font Stuff-----------------------------------------
	al_init_font_addon();
	al_init_ttf_addon();

	ALLEGRO_FONT *font = al_load_font("comic.ttf", 20, 0);
	//---------------------------------------------------------------------------------------------

	//----------------------------Allegro Mouse/Keyboard Stuff-------------------------------------
	al_install_mouse();

	al_install_keyboard();
	//---------------------------------------------------------------------------------------------

	//---------------------------------Allegro Timer Stuff-----------------------------------------
	ALLEGRO_TIMER *timer = NULL;
	int desiredFps = 60;
	int fpsDisplayUpdateTime = 10;

	timer = al_create_timer(1.0 / desiredFps);
	//---------------------------------------------------------------------------------------------
	
	//---------------------------------Allegro Event Stuff-----------------------------------------
	ALLEGRO_EVENT_QUEUE *event_queue = NULL;
	event_queue = al_create_event_queue();

	al_register_event_source(event_queue, al_get_mouse_event_source());
	al_register_event_source(event_queue, al_get_display_event_source(display));
	al_register_event_source(event_queue, al_get_keyboard_event_source());
	al_register_event_source(event_queue, al_get_timer_event_source(timer));
	//---------------------------------------------------------------------------------------------

	//-------------------------------------"Game" Loop---------------------------------------------
	bool loop = true;
	bool mapChanged = true;
	bool timeToDraw = true;

	Decider isMovingUp = NO;
	Decider isMovingDown = NO;
	Decider isMovingLeft = NO;
	Decider isMovingRight = NO;

	Image tileToDraw = GRASS;

	int timerTicks = 0;
	int gameTicks = 0;
	int lastDirection = DOWN;
	int mapOffsetX = mapSize/2 - (displayWidthInTiles/2);
	int mapOffsetY = mapSize/2 - (displayHeightInTiles/2);
	int editingMode = 0;

	al_start_timer(timer);
	
	//al_play_sample(mapMusic, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_LOOP, NULL);
	
	while (loop)
	{
		if (isMovingUp == YES && timeToDraw)
		{
			if (map->currentLocation->nodeUp->canEnter == YES)
			{
				mapOffsetY = mapOffsetY - 1;
				map->currentLocation = map->currentLocation->nodeUp;
			}

			else
			{
				isMovingUp = NO;
				al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
			}
			system("cls");
		}

		else if (isMovingDown == YES && timeToDraw)
		{
			if (map->currentLocation->nodeDown->canEnter == YES)
			{
				mapOffsetY = mapOffsetY + 1;
				map->currentLocation = map->currentLocation->nodeDown;
			}

			else
			{
				isMovingDown = NO;
				al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
			}
			system("cls");
		}

		else if (isMovingLeft == YES && timeToDraw)
		{
			if (map->currentLocation->nodeLeft->canEnter == YES)
			{
				mapOffsetX = mapOffsetX - 1;
				map->currentLocation = map->currentLocation->nodeLeft;
			}

			else
			{
				isMovingLeft = NO;
				al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
			}
			system("cls");
		}

		else if (isMovingRight == YES && timeToDraw)
		{
			if (map->currentLocation->nodeRight->canEnter == YES)
			{
				mapOffsetX = mapOffsetX + 1;
				map->currentLocation = map->currentLocation->nodeRight;
			}

			else
			{
				isMovingRight = NO;
				al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
			}
			system("cls");
		}

		//---------------------------------Draw The Picture----------------------------------------
		if (mapChanged && timeToDraw)
		{
			gameTicks++;
			al_clear_to_color(al_map_rgb(0, 0, 0));

			for (int a = 0; a < displayHeightInTiles; a++)
			{
				for (int b = 0; b < displayWidthInTiles; b++)
				{
					int k = a + mapOffsetY;
					int j = b + mapOffsetX;

					if (map->nodeArray[k][j]->tile == GRASS)
					{
						al_draw_bitmap(grassBitmap, tileWidth*b, tileHeight*a, 0);
					}

					else if (map->nodeArray[k][j]->tile == PATH)
					{
						al_draw_bitmap(pathBitmap, tileWidth*b, tileHeight*a, 0);
					}

					else if (map->nodeArray[k][j]->tile == WATER)
					{
						al_draw_bitmap(waterBitmap, tileWidth*b, tileHeight*a, 0);
					}

					else if (map->nodeArray[k][j]->tile == LAVA)
					{
						al_draw_bitmap(lavaBitmap, tileWidth*b, tileHeight*a, 0);
					}

				}
			}

			for (int a = 0; a < displayHeightInTiles; a++)
			{
				for (int b = 0; b < displayWidthInTiles; b++)
				{
					int k = a + mapOffsetY;
					int j = b + mapOffsetX;

					if (map->nodeArray[k][j]->canInteract == YES)
					{
						al_draw_tinted_bitmap(greenCircle, al_map_rgba_f(1, 1, 1, 0.5), tileWidth*b, tileHeight*a, 0);
					}
				}
			}

			for (int a = 0; a < displayHeightInTiles; a++)
			{
				for (int b = 0; b < displayWidthInTiles; b++)
				{
					int k = a + mapOffsetY;
					int j = b + mapOffsetX;

					if (map->nodeArray[k][j]->canEnter == NO)
					{
						al_draw_tinted_bitmap(redX, al_map_rgba_f(1, 1, 1, 0.5), tileWidth*b, tileHeight*a, 0);
					}
				}
			}

			al_draw_scaled_bitmap(blankBitmap, 0, 0, tileWidth, tileHeight, displayWidth - tileWidth*0.55, 0, tileWidth*0.55, tileHeight*0.55, 0);

			al_draw_text(font, al_map_rgb(0, 255, 0), displayWidth - tileWidth*0.55 + 15, 0, 0, "FPS:");

			string tmp = to_string(gameTicks*(desiredFps/fpsDisplayUpdateTime));
			char tab2[64];
			strcpy_s(tab2, tmp.c_str());

			al_draw_text(font, al_map_rgb(0, 255, 0), displayWidth - tileWidth*0.55 + 30, 25, 0, tab2);

			al_draw_scaled_bitmap(blankBitmap, 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.55, tileHeight*0.55, 0);

			if (editingMode == 0)
			{
				if (tileToDraw == GRASS)
				{
					al_draw_scaled_bitmap(grassBitmap, 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.5, tileHeight*0.5, 0);
				}

				else if (tileToDraw == PATH)
				{
					al_draw_scaled_bitmap(pathBitmap, 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.5, tileHeight*0.5, 0);
				}

				else if (tileToDraw == WATER)
				{
					al_draw_scaled_bitmap(waterBitmap, 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.5, tileHeight*0.5, 0);
				}

				else if (tileToDraw == LAVA)
				{
					al_draw_scaled_bitmap(lavaBitmap, 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.5, tileHeight*0.5, 0);
				}
			}

			else if (editingMode == 1)
			{
				al_draw_tinted_scaled_bitmap(redX, al_map_rgba_f(1, 1, 1, 0.5), 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.5, tileHeight*0.5, 0);
			}

			else if (editingMode == 2)
			{
				al_draw_tinted_scaled_bitmap(greenCircle, al_map_rgba_f(1, 1, 1, 0.5), 0, 0, tileWidth, tileHeight, 0, 0, tileWidth*0.5, tileHeight*0.5, 0);
			}

			string temp2 = "(";
			tmp = to_string(mapOffsetX + 1 + (displayWidthInTiles / 2));
			temp2.append(tmp);
			temp2.append(",");
			tmp = to_string(mapOffsetY + 1 + (displayHeightInTiles / 2));
			temp2.append(tmp);
			temp2.append(")");
			strcpy_s(tab2, temp2.c_str());

			al_draw_scaled_bitmap(blankBitmap, 0, 0, tileWidth, tileHeight, displayWidth/2, -(tileHeight*0.25), tileWidth*0.5, tileHeight*0.5, 0);
			al_draw_scaled_bitmap(blankBitmap, 0, 0, tileWidth, tileHeight, (displayWidth / 2) - tileWidth*0.5, -(tileHeight*0.25), tileWidth*0.5, tileHeight*0.5, 0);
			al_draw_text(font, al_map_rgb(255, 0, 0), displayWidth/2 - tileWidth*0.5 + 35, 0, 0, tab2);

			if (lastDirection == DOWN)
			{
				al_draw_bitmap(characterDownBitmap, (displayWidth / 2) - (tileWidth / 2), (displayHeight / 2) - (tileHeight / 2) - 25, 0);
			}

			else if (lastDirection == UP)
			{
				al_draw_bitmap(characterUpBitmap, (displayWidth / 2) - (tileWidth / 2), (displayHeight / 2) - (tileHeight / 2) - 25, 0);
			}

			else if (lastDirection == LEFT)
			{
				al_draw_bitmap(characterLeftBitmap, (displayWidth / 2) - (tileWidth / 2), (displayHeight / 2) - (tileHeight / 2) - 25, 0);
			}

			else if (lastDirection == RIGHT)
			{
				al_draw_bitmap(characterRightBitmap, (displayWidth / 2) - (tileWidth / 2), (displayHeight / 2) - (tileHeight / 2) - 25, 0);
			}

			al_flip_display();

			timeToDraw = false;
		}
		//-----------------------------------------------------------------------------------------

		//--------------------------------Event Handling-------------------------------------------
		mapChanged = false; 

		ALLEGRO_EVENT ev;
		al_wait_for_event(event_queue, &ev);

		if (ev.type == ALLEGRO_EVENT_TIMER)
		{
			timerTicks++;

			if (timerTicks == fpsDisplayUpdateTime)
			{
				mapChanged = true;
				timeToDraw = true;

				timerTicks = 0;
				gameTicks = 0;	
			}
		}

		if (ev.type == ALLEGRO_EVENT_MOUSE_BUTTON_DOWN)
		{
			if (editingMode == 0)
			{
				if (ev.mouse.button == 1)
				{
					int tileX;
					int tileY;

					tileX = ev.mouse.x / (int)tileWidth;
					tileY = ev.mouse.y / (int)tileHeight;

					map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->tile = tileToDraw;
				}

				else if (ev.mouse.button == 2)
				{
					tileToDraw = static_cast<Image>((tileToDraw + 1) % 4);
				}

				else if (ev.mouse.button == 3)
				{
					editingMode = 1;
				}

				mapChanged = true;
			}

			else if (editingMode == 1)
			{
				int tileX;
				int tileY;

				tileX = ev.mouse.x / (int)tileWidth;
				tileY = ev.mouse.y / (int)tileHeight;

				if (ev.mouse.button == 3)
				{
					editingMode = 0;
				}

				else if (ev.mouse.button == 1)
				{
					if (map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canEnter == YES)
					{
						map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canEnter = NO;
					}

					else if (map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canEnter == NO)
					{
						map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canEnter = YES;
					}
				}

				else if (ev.mouse.button == 2)
				{
					editingMode = 2;
				}

				mapChanged = true;
			}

			else if (editingMode == 2)
			{
				int tileX;
				int tileY;

				tileX = ev.mouse.x / (int)tileWidth;
				tileY = ev.mouse.y / (int)tileHeight;

				if (ev.mouse.button == 3)
				{
					editingMode = 0;
				}

				else if (ev.mouse.button == 1)
				{
					if (map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canInteract == YES)
					{
						map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canInteract = NO;
					}

					else if (map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canInteract == NO)
					{
						map->nodeArray[tileY + mapOffsetY][tileX + mapOffsetX]->canInteract = YES;
					}
				}

				else if (ev.mouse.button == 2)
				{
					editingMode = 1;
				}

				mapChanged = true;
			}
		}

		else if (ev.type == ALLEGRO_EVENT_KEY_DOWN)
		{	
			if (ev.keyboard.keycode == ALLEGRO_KEY_UP || ev.keyboard.keycode == ALLEGRO_KEY_W)
			{
				if (lastDirection == UP)
				{
					if (map->currentLocation->nodeUp->canEnter == YES)
					{
						isMovingUp = YES;
					}	

					else
					{
						al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
					}
				}

				else
				{
					isMovingUp = YES;
					lastDirection = UP;
					isMovingDown = NO;
					isMovingLeft = NO;
					isMovingRight = NO;
				}
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_DOWN || ev.keyboard.keycode == ALLEGRO_KEY_S)
			{
				if (lastDirection == DOWN)
				{
					if (map->currentLocation->nodeDown->canEnter == YES)
					{
						isMovingDown = YES;
					}

					else
					{
						al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
					}
				}

				else
				{
					isMovingDown = YES;
					lastDirection = DOWN;
					isMovingUp = NO;
					isMovingLeft = NO;
					isMovingRight = NO;
				}
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_LEFT || ev.keyboard.keycode == ALLEGRO_KEY_A)
			{
				if (lastDirection == LEFT)
				{
					if (map->currentLocation->nodeLeft->canEnter == YES)
					{
						isMovingLeft = YES;
					}

					else
					{
						al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
					}
				}

				else
				{
					isMovingLeft = YES;
					lastDirection = LEFT;
					isMovingUp = NO;
					isMovingDown = NO;
					isMovingRight = NO;
				}
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_RIGHT || ev.keyboard.keycode == ALLEGRO_KEY_D)
			{
				if (lastDirection == RIGHT)
				{
					if (map->currentLocation->nodeRight->canEnter == YES)
					{
						isMovingRight = YES;
					}

					else
					{
						al_play_sample(bumpSound, 0.75, 0.0, 1.0, ALLEGRO_PLAYMODE_ONCE, NULL);
					}
				}

				else
				{
					isMovingRight = YES;
					lastDirection = RIGHT;
					isMovingUp = NO;
					isMovingDown = NO;
					isMovingLeft = NO;
				}
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_ENTER)
			{
				if ((lastDirection == UP && map->currentLocation->nodeUp->canInteract == YES) ||
					(lastDirection == DOWN && map->currentLocation->nodeDown->canInteract == YES) ||
					(lastDirection == LEFT && map->currentLocation->nodeLeft->canInteract == YES) ||
					(lastDirection == RIGHT && map->currentLocation->nodeRight->canInteract == YES))
				{
					if (lastDirection == UP)
					{
						if (map->currentLocation->nodeUp->tile == WATER)
						{
							system("cls");
							cout << interactionLookupTable[0] << endl;
						}

						else if (map->currentLocation->nodeUp->tile == LAVA)
						{
							system("cls");
							cout << interactionLookupTable[1] << endl;
						}
					}

					else if (lastDirection == DOWN)
					{
						if (map->currentLocation->nodeDown->tile == WATER)
						{
							system("cls");
							cout << interactionLookupTable[0] << endl;
						}

						else if (map->currentLocation->nodeDown->tile == LAVA)
						{
							system("cls");
							cout << interactionLookupTable[1] << endl;
						}
					}

					else if (lastDirection == LEFT)
					{
						if (map->currentLocation->nodeLeft->tile == WATER)
						{
							system("cls");
							cout << interactionLookupTable[0] << endl;
						}

						else if (map->currentLocation->nodeLeft->tile == LAVA)
						{
							system("cls");
							cout << interactionLookupTable[1] << endl;
						}
					}

					else if (lastDirection == RIGHT)
					{
						if (map->currentLocation->nodeRight->tile == WATER)
						{
							system("cls");
							cout << interactionLookupTable[0] << endl;
						}

						else if (map->currentLocation->nodeRight->tile == LAVA)
						{
							system("cls");
							cout << interactionLookupTable[1] << endl;
						}
					}
				}
			}

			mapChanged = true;
		}

		else if (ev.type == ALLEGRO_EVENT_DISPLAY_CLOSE)
		{
			loop = false;
		}

		else if (ev.type == ALLEGRO_EVENT_KEY_UP)
		{
			if (ev.keyboard.keycode == ALLEGRO_KEY_ESCAPE)
			{
				loop = false;
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_UP || ev.keyboard.keycode == ALLEGRO_KEY_W)
			{
				isMovingUp = NO;
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_DOWN || ev.keyboard.keycode == ALLEGRO_KEY_S)
			{
				isMovingDown = NO;
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_LEFT || ev.keyboard.keycode == ALLEGRO_KEY_A)
			{
				isMovingLeft = NO;
			}

			else if (ev.keyboard.keycode == ALLEGRO_KEY_RIGHT || ev.keyboard.keycode == ALLEGRO_KEY_D)
			{
				isMovingRight = NO;
			}
		}
		//-----------------------------------------------------------------------------------------	
	}

	//---------------------------------------------------------------------------------------------

	//----------------------------------------Clean Up---------------------------------------------
	al_destroy_display(display);
	al_destroy_event_queue(event_queue);
	al_destroy_bitmap(grassBitmap);
	al_destroy_bitmap(pathBitmap);
	al_destroy_bitmap(waterBitmap);
	al_destroy_bitmap(blankBitmap);
	al_destroy_bitmap(lavaBitmap);
	al_destroy_bitmap(characterDownBitmap);
	al_destroy_bitmap(characterUpBitmap);
	al_destroy_bitmap(characterLeftBitmap);
	al_destroy_bitmap(characterRightBitmap);
	al_destroy_bitmap(redX);
	al_destroy_bitmap(greenCircle);
	al_destroy_timer(timer);
	al_destroy_sample(bumpSound);
	al_destroy_sample(mapMusic);

	system("cls");
	//---------------------------------------------------------------------------------------------

	return 0;
}