//
//  serialization_magicavoxel.h
//  particubes
//
//  Created by Adrien Duermael on 12/23/17.
//  Copyright © 2017 Particubes. All rights reserved.
//

#ifndef serialization_magicavoxel_h
#define serialization_magicavoxel_h

#include <stdbool.h>

#include "world.h"
#include "camera.h"


enum serialization_magicavoxel_error {
    none = 0,
    cant_open_file = 1,
    invalid_format = 2,
    pack_chunk_found = 3,
    unknown_chunk = 4,
};

/// Saves world in .vox to be opened in Magicavoxel
/// Returns empty string on success, error description otherwise
const char* serialization_magicavoxel_save_world(const World *world,
                                                 const uint8_t color,
                                                 const uint8_t backgroundColor,
                                                 const char* filepath);

// returns null on success
// error message otherwise
enum serialization_magicavoxel_error serialization_magicavoxel_import_world(const char* src,
                                                                            const char* dst,
                                                                            const void* imageData,
                                                                            uint32_t imageDataSize);

#endif /* serialization_magicavoxel_h */
