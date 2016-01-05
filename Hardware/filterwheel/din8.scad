din_margin = 0.4;

din_face_y = 15.7 + din_margin;
din_back_y = 16.7 + din_margin;
din_back_height_z_1 = 5.4;
din_back_height_z_2 = 8;
din_face_x = 15.3 + din_margin;
din_z = 16.3;
din_oreille_z_level = 11.1 - din_margin / 2;
din_oreille_z = 1.3 + din_margin;
din_oreille_marge_y = 0.8;
din_oreille_x = (17.8 + 2 * din_margin - din_face_x) / 2;

module din8()
{
    // x : width
    // y : height
    // z : profondeur
    cube([din_face_x, din_face_y, din_z]);
    hull() {
        cube([din_face_x, din_back_y,din_back_height_z_1]);
        translate([0, 0, din_back_height_z_2])
        cube([din_face_x, din_face_y,0.1]);
    }
    translate([-din_oreille_x,din_oreille_marge_y, din_oreille_z_level])
    cube([2 * din_oreille_x + din_face_x,
        din_face_y - 2 * din_oreille_marge_y, din_oreille_z]);
        


    // Un espace pour les cable
    color("blue")
    translate([0,0,-12])
    cube([din_face_x, din_back_y, 12]);
}


//din8();