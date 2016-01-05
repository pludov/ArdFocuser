use <28BYJ-48.scad>;
use <publicDomainGearV1.1.scad>;
use <MCAD/shapes.scad>;
include <din8.scad>;


m3_ecrou_size = 5.5 + 0.3;
m3_diam = 3.4;

// Les dimensions de la goulotte
ext_x = 40;
ext_y = 27.8;
ext_z = 10;
// Le din dépasse de ça
ressort=11;
goulotte_width=1.6;

%translate([0, 0, -40])
difference() {
    cube([ext_x, ext_y, 40]);
    translate([goulotte_width, goulotte_width, -0.1])
    cube([ext_x - 2 * goulotte_width, ext_y - 2 * goulotte_width, 40.2]);
}



module din8pos() {
    translate([goulotte_width + 3,ext_y - din_back_y- goulotte_width, ressort - din_z])
    children();

}


//epaisseur des parois
epaisseur = 1.0;
// diametre d'un cable moteur
diam_cable=1.4;

difference() {
    union() {
        translate([0, 0, 0])
        cube([ext_x, ext_y, ext_z]);
    
        // Sur le capot
        translate([0,-epaisseur,-22])
            cube([ext_x ,epaisseur, 22 + ext_z]);

        // Coté
        translate([-epaisseur, -epaisseur, -22])
            cube([epaisseur, ext_y + epaisseur, 22 + ext_z]);

        translate([ext_x, -epaisseur, -22])
            cube([epaisseur, ext_y + epaisseur, 22 + ext_z]);
    }
    
    // La prise din
    minkowski() {
       din8pos()
        din8();
        hull() {
            sphere(size=0.4);
            translate([0,10,0])
            sphere(size=0.4);
        }
    }
    
        
    // L'angle en alu
    color("blue")
    translate([2,ext_y - 2.5, 1])
    {
        hull() {
            cube([0.1, 0.1, 20]);
            translate([0,+10, 0])
            cube([0.1, 0.1, 20]);
            translate([10 * sin(60), 10*cos(60), 0])
            cube([0.1, 0.1, 20]);
        }
    }

    // Les cables moteurs
    translate([29, ext_y-goulotte_width - diam_cable / 2, 0])
    minkowski() {
        rotate([-30, 0, 0])
        for(i=[0:4]) {
            translate([i * diam_cable, 0, -2])
            cylinder(d=diam_cable + 0.2, h=10);
        }
        cube([0.1, 5, 0.1]);
    }

    // La visse de fixation
    translate([ext_x / 2, goulotte_width - 0.1, -6])
    {
        rotate([-90, 0, 0])
        {
            translate([0, 0, 1.3])
            hexagon(m3_ecrou_size, 2.6);
            translate([0, 0, -5])
            cylinder(d=m3_diam + 0.3, h=10,$fn=32);
        }
    }
}

// Le serre ecrou
difference() {
    translate([goulotte_width + 0.2, goulotte_width + 0.2, -11])
    cube([ext_x - 2 * (goulotte_width + 0.2), 3.2, 10]);
    translate([ext_x / 2, goulotte_width - 0.1, -6])
    {
        rotate([-90, 0, 0])
        {
            translate([0, 0, 1.3])
            hexagon(m3_ecrou_size, 2.6);
            translate([0, 0, -5])
            cylinder(d=m3_diam + 0.3, h=10,$fn=32);
        }
    }
}
    


din8pos()
din8();