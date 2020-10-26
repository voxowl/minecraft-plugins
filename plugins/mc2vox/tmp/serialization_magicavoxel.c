//
//  serialization_magicavoxel.c
//  particubes
//
//  Created by Adrien Duermael on 12/23/17.
//  Copyright © 2017 Particubes. All rights reserved.
//

#include "serialization.h"
#include "serialization_magicavoxel.h"

#include <string.h>
#include <math.h>

// Returns 0 on success, 1 otherwise.
// This function doesn't close the file descriptor, you probably want to close
// it in the calling context, when an error occurs.
uint8_t readVoxMagicBytes(FILE* fd) {
    const char* magic_bytes = "VOX ";
    int magic_bytes_size = 4;
    
    char current = 0;
    for (int i = 0; i < magic_bytes_size; i++) {
        size_t n = fread(&current, sizeof(char), 1, fd);
        if (n != 1) {
            printf("💾 💥 failed to read magic byte\n");
            return 1; // error
        }
        if (current != magic_bytes[i]) {
            printf("💾 💥 incorrect magic bytes\n");
            return 1; // error
        }
    }
    return 0; // ok
}

// Some chunks in .vof files are not supported in Particubes (materials, transformations, etc.)
// But we have to save them in case .particubes files end up being re-exported into .vox
uint8_t chunk_not_supported_but_saved(FILE* fd, const char* chunk_name, uint32_t* content_bytes, uint32_t* children_content_bytes, World* world) {
    
    // Let's save all unknown chunks without even checking what they are.
    
    //    if (strcmp(chunk_name, "nTRN") == 0 || strcmp(chunk_name, "nSHP") == 0 ||
    //        strcmp(chunk_name, "nGRP") == 0 || strcmp(chunk_name, "MATL") == 0 ||
    //        strcmp(chunk_name, "MATT") == 0 || strcmp(chunk_name, "LAYR") == 0 ||
    //        strcmp(chunk_name, "rLIT") == 0 || strcmp(chunk_name, "rAIR") == 0 ||
    //        strcmp(chunk_name, "rLEN") == 0 || strcmp(chunk_name, "POST") == 0 ||
    //        strcmp(chunk_name, "rDIS") == 0 || strcmp(chunk_name, "rOBJ") == 0 ) {}
    
    if (readUint32(fd, content_bytes) != 0) {
        printf("💾 💥 can't read current chunk's size\n");
        return invalid_format;
    }
    
    if (readUint32(fd, children_content_bytes) != 0) {
        printf("💾 💥 can't read current chunk children's size\n");
        return invalid_format;
    }
    
    // 12: name (4) + size uint32 (4) + children size uint32 (4)
    if (rewindBytes(fd, 12) != 0) {
        printf("couldn't rewind source metadata\n");
        return invalid_format;
    }
    
    size_t totalSize = *content_bytes + *children_content_bytes + 12;
    world_append_metadata(world, world_metadata_magicavoxel_main_chunk_children, totalSize, fd);
    
    return none;
    
    // return unknown_chunk;
}


enum serialization_magicavoxel_error serialization_magicavoxel_import_world(const char* src,
                                                                            const char* dst,
                                                                            const void* imageData,
                                                                            uint32_t imageDataSize) {
    
    // open file
    FILE* fd = fopen(src, "rb");
    if (fd == NULL) {
        printf("💾 💥 can't open file (%s)\n", src);
        return cant_open_file;
    }
    
    // read magic bytes
    if (readVoxMagicBytes(fd) != 0) {
        printf("💾 💥 failed to read magic bytes (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    
    // read file format
    uint32_t fileFormatVersion = 0;
    if (readUint32(fd, &fileFormatVersion) != 0) {
        printf("💾 💥 failed to read file format version (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    
    // read MAIN chunk
    
    size_t chunkNameSize = 4;
    char chunkName[chunkNameSize + 1];
    
    if (readString(fd, chunkNameSize, chunkName) != 0) {
        printf("💾 💥 failed to read Magicavoxel file (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    
    if (strcmp(chunkName, "MAIN") != 0) {
        printf("💾 💥 MAIN chunk not found (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    
    // read main chunk info
    
    uint32_t main_chunk_content_bytes;
    uint32_t main_chunk_children_content_bytes;
    
    if (readUint32(fd, &main_chunk_content_bytes) != 0) {
        printf("💾 💥 can't read MAIN chunk content size (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    // MAIN chunk shouldn't store data directly, it's only there to
    // reference children.
    if (main_chunk_content_bytes > 0) {
        printf("💾 💥 MAIN chunk content size > 0 (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    
    if (readUint32(fd, &main_chunk_children_content_bytes) != 0) {
        printf("💾 💥 can't read MAIN chunk children content size (%s)\n", src);
        fclose(fd);
        return invalid_format; // error
    }
    
    // It really looks like a .vox file...
    // Let's create a Particubes world! :)
    
    World *world = world_new();
    
    // set default values
    
    world_set_lightEnabled(world, LIGHT_DEFAULT_ENABLED); // light enabled
    world_light_set_locked(world, LIGHT_DEFAULT_LOCKEDTOCREATION);
    // world_light_set_y_rotation(world, lightLeftRightRotation); // camera->_rightLeftRotation + LIGHT_DEFAULT_ROTATION_LEFT_RIGHT
    world_light_set_x_rotation(world, LIGHT_DEFAULT_ROTATION_UP_DOWN); // top/bottom
    
    world_set_ambient_occlusion(world, AMBIENT_OCCLUSION_DEFAULT_ENABLED);
    world_set_isometric(world, ISOMETRIC_DEFAULT_ENABLED);
    
    // read chunks
    
    uint32_t current_chunk_content_bytes;
    uint32_t current_chunk_children_content_bytes;
    
    uint32_t to_read = main_chunk_children_content_bytes;
    
    while(to_read > 0) {
        if (readString(fd, chunkNameSize, chunkName) != 0) {
            printf("💾 💥 failed to read Magicavoxel file (%s)\n", src);
            fclose(fd);
            return invalid_format; // error
        }
        to_read -= chunkNameSize;
        
        // PACK
        if (strcmp(chunkName, "PACK") == 0) {
            if (readUint32(fd, &current_chunk_content_bytes) != 0) {
                printf("💾 💥 can't read current chunk's size (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= 4;
            
            if (readUint32(fd, &current_chunk_children_content_bytes) != 0) {
                printf("💾 💥 can't read current chunk children's size (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= 4;
            
            uint32_t nbModels;
            if (readUint32(fd, &nbModels) != 0) {
                printf("💾 💥 can't read PACK chunk (%s)\n", src);
                world_free(world);
                fclose(fd);
                return pack_chunk_found; // error
            }
            
            if (nbModels > 1) {
                printf("💾 💥 PACK with more than 1 model not supported (%s)\n", src);
                world_free(world);
                fclose(fd);
                return pack_chunk_found; // error
            }
            
            to_read -= current_chunk_content_bytes;
            to_read -= current_chunk_children_content_bytes;
        }
        // SIZE
        else if (strcmp(chunkName, "SIZE") == 0) {
            uint32_t chunk_size = 4+4+12;
            
            // skip information, model size will be defined dynamically
            // adding voxels from XYZI chunk.
            if (skipBytes(fd, chunk_size) != 0) {
                printf("💾 💥 less bytes than expected in SIZE chunk (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= chunk_size;
        }
        // XYZI
        else if (strcmp(chunkName, "XYZI") == 0) {
            if (readUint32(fd, &current_chunk_content_bytes) != 0) {
                printf("💾 💥 can't read current chunk's size (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= 4;
            
            if (readUint32(fd, &current_chunk_children_content_bytes) != 0) {
                printf("💾 💥 can't read current chunk children's size (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= 4;
            
            uint32_t nbVoxels;
            uint8_t x, y, z, color_index;
            
            if (readUint32(fd, &nbVoxels) != 0) {
                printf("💾 💥 can't read number voxels in XYZI chunk (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            
            for (int i = 0; i < nbVoxels; i++) {
                // ⚠️ y -> z, z -> y
                
                uint8_t err = readUint8(fd, &x) + readUint8(fd, &z) +
                readUint8(fd, &y) + readUint8(fd, &color_index);
                
                if (err != 0) {
                    printf("💾 💥 can't read XYZI voxel (%s)\n", src);
                    world_free(world);
                    fclose(fd);
                    return invalid_format; // error
                }
                
                /*
                 Not totally getting it still...
                 But color index is shifted by 1 so we have to compensate.
                 It's also done when exporting .vox (+1 instead of -1)
                 
                 * color [0-254] are mapped to palette index [1-255], e.g :
                 |
                 | for ( int i = 0; i <= 254; i++ ) {
                 |     palette[i + 1] = ReadRGBA();
                 | }
                 */
                world_add_block_with_color(world, color_index - 1, x, y, z, false, NULL, symmetry_type_none, NULL);
            }
            
            to_read -= current_chunk_content_bytes;
            to_read -= current_chunk_children_content_bytes;
            
        }
        // RGBA (palette)
        else if (strcmp(chunkName, "RGBA") == 0) {
            
            if (readUint32(fd, &current_chunk_content_bytes) != 0) {
                printf("💾 💥 can't read current chunk's size (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= 4;
            
            if (readUint32(fd, &current_chunk_children_content_bytes) != 0) {
                printf("💾 💥 can't read current chunk children's size (%s)\n", src);
                world_free(world);
                fclose(fd);
                return invalid_format; // error
            }
            to_read -= 4;
            
            // there is ALWAYS 256 colors apparently...
            PaletteType1* p = palette_new(8, 32);
            uint8_t r, g, b, a;
            
            for (int i = 0; i < 256; i++) {
                
                uint8_t err = readUint8(fd, &r) + readUint8(fd, &g) +
                readUint8(fd, &b) + readUint8(fd, &a);
                
                if (err != 0) {
                    printf("💾 💥 can't read RGBA component (%s)\n", src);
                    world_free(world);
                    fclose(fd);
                    return invalid_format; // error
                }
                
                palette_set_color(p, i, (PCColorType1) {.r = r, .g = g, .b = b, .a = a} );
            }
            
            world_set_palette(world, p);
            palette_destroy(p);
            
            to_read -= current_chunk_content_bytes;
            to_read -= current_chunk_children_content_bytes;
        }
        else if (chunk_not_supported_but_saved(fd, chunkName, &current_chunk_content_bytes, &current_chunk_children_content_bytes, world) == none) {
            // nothing to do, everything done in chunk_not_supported_but_saved
            to_read -= 4;
            to_read -= 4;
            to_read -= current_chunk_content_bytes;
            to_read -= current_chunk_children_content_bytes;
        }
        // UNKNOWN CHUNK
        else {
            printf("--- 👁 UNKNOWN\n");
            printf("💾 💥 UNKNOWN type of chunk: %s (%s)\n", chunkName, src);
            world_free(world);
            fclose(fd);
            return invalid_format; // error
        }
        
        if (feof(fd)) { break; }
    }
    
    Camera *camera = camera_new();
    
    // now that the world has been populated, let's see about its size & origin
    // to place the camera.
    SHAPE_SIZE_INT_T size_x;
    SHAPE_SIZE_INT_T size_y;
    SHAPE_SIZE_INT_T size_z;
    
    SHAPE_COORDS_INT_T origin_x;
    SHAPE_COORDS_INT_T origin_y;
    SHAPE_COORDS_INT_T origin_z;
    
    if (world_get_3d_size_and_origin(world, &size_x, &size_y, &size_z, &origin_x, &origin_y, &origin_z)) {
        // look at center of world, and edge on z
        camera_set_target(camera, origin_x + size_x * 0.5, origin_y + size_y * 0.5, origin_z);
        
        // define right distance to see the entire model
        SHAPE_SIZE_INT_T bigger_side = size_x > size_y ? size_x : size_y;
        float alpha = 65.0 * 0.5 * PI / 180.0;
        float distance_from_target = (float)bigger_side * 0.7 / tanf(alpha);
        
        camera_with_target_set_distance_from_target(camera, distance_from_target);
    }
    
    uint8_t selectedColor = 0;
    uint8_t background = 0;
    
    serialization_save_world(world, camera, selectedColor, background, imageData, imageDataSize, dst);
    // tmp
    world_free(world);
    camera_free(camera);
    
    fclose(fd);
    return none;
}




const char* serialization_magicavoxel_save_world(const World *world,
                                          const uint8_t color,
                                          const uint8_t backgroundColor,
                                          const char* filepath) {
    
    SHAPE_SIZE_INT_T world_x;
    SHAPE_SIZE_INT_T world_y;
    SHAPE_SIZE_INT_T world_z;
    SHAPE_COORDS_INT_T origin_x;
    SHAPE_COORDS_INT_T origin_y;
    SHAPE_COORDS_INT_T origin_z;
    
    
    world_get_3d_size_and_origin(world, &world_x, &world_y, &world_z,
                                 &origin_x, &origin_y, &origin_z);
    
    // shift is used to compensate negative origin != (0,0,0)
    // .vox does not support negative coordinates
    // and I believe the first block is always at (0,0,0)
    SHAPE_COORDS_INT_T shift_x = -origin_x;
    SHAPE_COORDS_INT_T shift_y = -origin_y;
    SHAPE_COORDS_INT_T shift_z = -origin_z;
    
    if (world_x > 256 || world_y > 256 || world_z > 256) {
        printf("💾 world is too big, can't export for magicavoxel\n");
        return "ParticubesErrorMagicavoxelExportTooBig";
    }
    
    // validate arguments
    if (world == NULL) {
        printf("💾 💥 world pointer is NULL\n");
        return "ParticubesErrorMagicavoxelExport";
    }
    
    if (filepath == NULL) {
        printf("💾 💥 filepath pointer is NULL\n");
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // open file
    FILE* fd = fopen(filepath, "wb");
    if (fd == NULL) {
        printf("💾 💥 can't open file (%s)\n", filepath);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // write 'VOX '
    if (fwrite("VOX ", sizeof(char), 4, fd) != 4) {
        printf("💾 💥 failed to write 'VOX ' (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // version number
    uint32_t format = 150;
    if (fwrite(&format, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write file format (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // MAIN chunk
    if (fwrite("MAIN", sizeof(char), 4, fd) != 4) {
        printf("💾 💥 failed to write 'MAIN' (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of MAIN chunk content: 0 (actual content is in children)
    uint32_t zero_bytes = 0;
    if (fwrite(&zero_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write MAIN size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of children
    // - CHUNK HEADER: 4 + 4 + 4 = 12
    // - SIZE: 12 + 12 = 24
    // - XYZI: 12 + 4 + 4 x nb_blocks
    // - RGBA depends on palette
    
    size_t nb_blocks = shape_get_nb_blocks(world_get_shape(world));
    
    uint32_t chunk_header_bytes = 12;
    uint32_t size_bytes = 12;
    uint32_t xyzi_bytes = 4 + 4 * (uint32_t)(nb_blocks);
    uint32_t rgba_bytes = 256 * 4;
    
    // source metadata not handled by Particubes, but good to put it back in the .vox
    size_t unkown_chunk_bytes = world_metadata_size_for_key(world, world_metadata_magicavoxel_main_chunk_children);
    
    uint32_t children_bytes = chunk_header_bytes + size_bytes + chunk_header_bytes + xyzi_bytes + chunk_header_bytes + rgba_bytes + (uint32_t)unkown_chunk_bytes;
    
    if (fwrite(&children_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write MAIN children size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // no PACK chunk
    
    // SIZE chunk
    if (fwrite("SIZE", sizeof(char), 4, fd) != 4) {
        printf("💾 💥 failed to write 'MAIN' (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of SIZE chunk
    if (fwrite(&size_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write MAIN size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of SIZE children
    if (fwrite(&zero_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write MAIN size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // blocks
    
    // x
    uint32_t x = (uint32_t)world_x;
    if (fwrite(&x, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write SIZE x (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // y
    uint32_t y = (uint32_t)world_z;
    if (fwrite(&y, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write SIZE y (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // z
    uint32_t z = (uint32_t)world_y;
    if (fwrite(&z, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write SIZE x (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    
    // XYZI chunk
    if (fwrite("XYZI", sizeof(char), 4, fd) != 4) {
        printf("💾 💥 failed to write 'XYZI' (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of XYZI chunk
    if (fwrite(&xyzi_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write XYZI size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of XYZI children
    if (fwrite(&zero_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write XYZI children size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // XYZI: nb voxels
    uint32_t n = (uint32_t)nb_blocks;
    if (fwrite(&n, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write XYZI nb voxels (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // loop over blocks
    
    Chunk *chunk = NULL;
    int3 *worldPos = int3_new(0, 0, 0);
    int3 *posInChunk = int3_new(0, 0, 0);
    Block* b = NULL;
    
    for (int k = 0; k < world_z; k++) {
        for (int j = 0; j < world_y; j++) {
            for (int i = 0; i < world_x; i++) {
                b = NULL;
                
                int3_set(worldPos, (i + origin_x), (j + origin_y), (k + origin_z));
                
                world_get_chunk_and_relative_position(world, worldPos, &chunk, NULL, posInChunk);
                if (chunk != NULL) {
                    b = chunk_get_block_2(chunk, posInChunk);
                }
                
                if (b == NULL) {
                    // no block, don't do anything
                } else {
                    uint16_t bci = block_get_color_index(b);
                    
                    // ⚠️ y -> z, z -> y
                    uint8_t x = (uint8_t)worldPos->x + shift_x;
                    uint8_t y = (uint8_t)worldPos->z + shift_z;
                    uint8_t z = (uint8_t)worldPos->y + shift_y;
                    uint8_t c = (uint8_t)bci + 1;
                    
                    // printf("block : %d, %d, %d - color: %d\n", x, y, z, c);
                    
                    if (fwrite(&x, sizeof(uint8_t), 1, fd) != 1) {
                        printf("💾 💥 failed to write x size (%s)\n", filepath);
                        fclose(fd);
                        return "ParticubesErrorMagicavoxelExport";
                    }
                    
                    if (fwrite(&y, sizeof(uint8_t), 1, fd) != 1) {
                        printf("💾 💥 failed to write y size (%s)\n", filepath);
                        fclose(fd);
                        return "ParticubesErrorMagicavoxelExport";
                    }
                    
                    if (fwrite(&z, sizeof(uint8_t), 1, fd) != 1) {
                        printf("💾 💥 failed to write z size (%s)\n", filepath);
                        fclose(fd);
                        return "ParticubesErrorMagicavoxelExport";
                    }
                    
                    if (fwrite(&c, sizeof(uint8_t), 1, fd) != 1) {
                        printf("💾 💥 failed to write c size (%s)\n", filepath);
                        fclose(fd);
                        return "ParticubesErrorMagicavoxelExport";
                    }
                }
            }
        }
    }
    
    // RGBA chunk
    
    // RGBA chunk
    if (fwrite("RGBA", sizeof(char), 4, fd) != 4) {
        printf("💾 💥 failed to write 'RGBA' (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of RGBA chunk
    if (fwrite(&rgba_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write RGBA size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    // size of RGBA children
    if (fwrite(&zero_bytes, sizeof(uint32_t), 1, fd) != 1) {
        printf("💾 💥 failed to write RGBA children size (%s)\n", filepath);
        fclose(fd);
        return "ParticubesErrorMagicavoxelExport";
    }
    
    uint8_t zero = 0;
    
    const PaletteType1 *palette = world_get_palette(world);
    uint16_t nbColors = palette->rows * palette->columns;
    
    for (int i = 0; i < 256; i++) {
        if (i < nbColors) {
            // r
            if (fwrite(&palette->colors[i].r, sizeof(uint8_t), 1, fd) != 1) {
                printf("💾 💥 failed to write r (%s)\n", filepath);
                fclose(fd);
                return "ParticubesErrorMagicavoxelExport";
            }
            // g
            if (fwrite(&palette->colors[i].g, sizeof(uint8_t), 1, fd) != 1) {
                printf("💾 💥 failed to write g (%s)\n", filepath);
                fclose(fd);
                return "ParticubesErrorMagicavoxelExport";
            }
            // b
            if (fwrite(&palette->colors[i].b, sizeof(uint8_t), 1, fd) != 1) {
                printf("💾 💥 failed to write b (%s)\n", filepath);
                fclose(fd);
                return "ParticubesErrorMagicavoxelExport";
            }
            // a
            if (fwrite(&palette->colors[i].a, sizeof(uint8_t), 1, fd) != 1) {
                printf("💾 💥 failed to write a (%s)\n", filepath);
                fclose(fd);
                return "ParticubesErrorMagicavoxelExport";
            }
        } else {
            for (int j = 0; j < 4; j++) {
                if (fwrite(&zero, sizeof(uint8_t), 1, fd) != 1) {
                    printf("💾 💥 failed to write empty color (%s)\n", filepath);
                    fclose(fd);
                    return "ParticubesErrorMagicavoxelExport";
                }
            }
        }
    }
    
    // unknown chunks (source metadata
    
    if (unkown_chunk_bytes > 0) {
        world_write_metadata(world, world_metadata_magicavoxel_main_chunk_children, true, fd);
    }
    

    fclose(fd);
    return "";
}
