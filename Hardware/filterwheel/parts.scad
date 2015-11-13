use <28BYJ-48.scad>;
use <publicDomainGearV1.1.scad>;
use <MCAD/shapes.scad>;

// conclusion 10 sept:
// Haut: profondeur écrou +1.5mm, ou alors, dans le bas aussi
// Haut: prendre au moins 8 de diam pour le support hall (indicateur_diam_ext)
// Haut & bas: Monter le moteur d'au moins 1.2 (stepper_height)
// Haut : Hall: réduire la distance, agrandir les trous, séparer les 3 pins
// fin conclusion 10 sept
stepper_axe_meplat = 3.2;
stepper_axe_diam = 5.1;

// Ecrou de 3x30: http://www.leroymerlin.fr/v3/p/produits/lot-de-25-boulons-tete-cylindrique-en-acier-zingue-long-30-x-diam-3-mm-e125287
// Extrusion pour les ecrous de 3
// FIXME: verifier
m3_diam = 3.2;
// FIXME: verifier
m3_tete_diam = 6;
// FIXME : verifier
m3_tete_length = 3;
//
// http://www.visseriefixations.fr/ecrous/ecrous-autofreines/ecrou-hexagonal-autofreine-bague-nylon/ecrou-nylstop-inox-a2-din-985.html
// Prendre "s"
// FIXME : verifier
m3_ecrou_size = 5.5 + 0.3; // 6.01 / 2 + 0.15;
// FIXME : verifier
m3_ecrou_length = 3;

// Deux morceaux principaux : la fixation de courroie sur la roue principale
// Celle ci doit être constituée de 5 partie qui devront être collée ensemble

// La distance du centre au coté non arrondi
ray_ext_min = 62.5;
// La distance du centre à l'arrondi
ray_ext_max = 66.5;

// La distance du centre au petit coté
opening_center_dist=50.5;
opening_angle = 90-83.66;


hauteur_externe = 21;

debord_hauteur = 4;

//epaisseur de la parois basse
low_width = 7;
//epaisseur de la parois haute
high_width = 4;

// Le rayon du caroussel
ray_carroussel = 54;
// La marge carrousel/bord (en exterieur)
marge_carroussel = 2;
// La distance du carroussel au bas (low_width)
// 
level_carroussel = hauteur_externe - 9.5;
width_carroussel = 4;

diam_filter = 31.75 + 0.3;
distant_filter_center_min = 30;
distant_filter_center_max = 32.4;

gear_mm_per_tooth=2.33;
gear_big_count = 145; // Il faudrait un multiple de 5 pour avoir des pièces identiques!
gear_small_count = 24;
gear_height = level_carroussel - low_width-0.8;
echo("gear_height:", gear_height);
// Le rayon au centre de la grosse roue 
gear_inner_radius = 25;



indicateur_center_distance = opening_center_dist / cos(opening_angle);
indicateur_diam_ext = 8.4;

// Le niveau bas du senseur
sensor_low_level = level_carroussel + width_carroussel + 0.4;
        
// On va surelever le senseur de ça (prévoir de la colle...)
magnet_hall_tolerance = 0.4;
magnet_center_distance = indicateur_center_distance;
magnet_diam = 4;
magnet_height = 2;
magnet_hall_support_width = 3;

capot_width = 2;
capot_height = 18;

// La forme est globalement un hull de 5 cercles. On connait le diametre de ces cercles
// a partir des rayont ext min et max

cs = cos(72/2);
raysec = (ray_ext_min - ray_ext_max*cs) / (1 - cs);

accroches = [
    [ 30, -52 ],
    [ -6, -59 ],
    [ 35, -75 ],
//    [ -18, -55 ]
];

accroches_capot = [
    [-22, -64],
    [3, -86.5],
    [39, -55]
];

// Le point "fixe" du moteur
accroche_moteur_angle=25;
accroche_moteur_dst_bord=11.5; // Le trou fait 1.5
accroche_moteur_x = sin(accroche_moteur_angle) * (ray_carroussel + marge_carroussel + accroche_moteur_dst_bord);
accroche_moteur_y = -cos(accroche_moteur_angle) * (ray_carroussel + marge_carroussel + accroche_moteur_dst_bord);

// Hauteur du moteur par rapport à la base de la RAF. Doit être plus long que le nez du moteur (10)
stepper_height = 12.2;
stepper_angle = -154 ;
stepper_angle_tol = 3; // +/- 5 deg pour l'accroche

// Nombre de steps pour avoir une découpe (prévoir assez haut à la cible (100 ?)! 
stepper_angle_step = 2;

// La roue à filtre, coupée
module roue_a_filtre() {
    difference() {
        hull() {
            for(i = [0:4]) {
                angle = i * 72;
                
                translate([ (ray_ext_max - raysec) * cos(angle),
                            (ray_ext_max - raysec) * sin(angle), 
                            0])
                cylinder(r = raysec, h = hauteur_externe);
            }
        }
        translate([0, 0, low_width])
        cylinder(r=ray_carroussel+marge_carroussel, h=hauteur_externe - low_width - high_width);
        translate([distant_filter_center_max, 0, -1]) 
            cylinder(r = 42/2, h = hauteur_externe + 2);
        
        // L'ouverture pour controle manuel
        rotate([0,0,opening_angle])
        hull()
        {
            translate([-200, -(opening_center_dist + 100), low_width])
                cube([400, 0.1, hauteur_externe]);
            
            translate([-200, -(opening_center_dist + 100), hauteur_externe])
                cube([400, 100, 0.1]);
            
            arrondi_d = 5;
            arrondi_prof = 0.5;
            
            translate([0, -opening_center_dist, low_width])
            translate([0, arrondi_prof -arrondi_d / 2, arrondi_d / 2])
            rotate([0, 90, 0])
            translate([0,0,-100])
            cylinder(d=arrondi_d, h=200, $fn=64);
            
        }
        
        for(accroche_pt = accroches) {
            translate([accroche_pt[0], accroche_pt[1], -10])
                cylinder(d=m3_diam, $fn=64, h=100);
        }
        
        translate([0, -opening_center_dist / cos(opening_angle), low_width + 0.1])
            cylinder(d=8, h=100);
    }
    *color("grey")
    translate([0, 0, level_carroussel])
        cylinder(r = ray_carroussel, h = width_carroussel);
}


*roue_a_filtre();
// Hall sensor
module hallSensor() {
    color("grey")
    union() {
        translate([0,0,-0.75/2])
        cube([4.10, 3.0, 0.75], center=true);
        hull() {
            translate([0,0,0.0])
            cube([4.10, 3.0, 0.01], center=true);
            translate([0,0,0.75])
            cube([2.62,2.9,0.01], center=true);
        }
    }
    color("lightgrey")
    for(x = [-1.27, 0, 1.27]) {
        translate([x, 0, 0])
        translate([-0.38/2,1.45,-0.38])
        cube([0.38, 14.5, 0.38]);
    }
}

corps_y0 = -39.5;
corps_y1 = 38.5;
module corps() {
    full_height = hauteur_externe + debord_hauteur + capot_height;
    y0 = corps_y0;
    y1 = corps_y1;
    rotate([0,0, opening_angle])
    difference() {
        union() {
            hull() {
                translate([y0, -opening_center_dist - 2, low_width+ 0.2])
                cube([y1 - y0, 1.8, full_height - 0.2]);
                
                hull() {
                    translate([-8, -72, low_width + 0.2])
                    cylinder(d=35, h=full_height - 0.2);
                    translate([12, -75, low_width + 0.2])
                    cylinder(d=45, h=full_height- 0.2);
                }
            }
            translate([ 
                    y0,
                    -opening_center_dist - 0.2, 
                    hauteur_externe +0.2])
                cube([y1-y0, 15, debord_hauteur + capot_height - 0.2]);
            
            
            
        }
        translate([0,-opening_center_dist, low_width])
            difference() {
                rabot = 4;
                cube([100, 2 * rabot, 2 * rabot], center=true);
                translate([0,-rabot,rabot])
                rotate([0,90,0])
                translate([0,0,-52])
                cylinder(r=rabot, h=104, $fn=64);
            }
    }
}



// La "grande roue"
// projection()
*translate([0,0, level_carroussel -gear_height ]) {
    color("red")
    difference()  {
        translate([0,0,gear_height / 2 + 0.1])
        gear(gear_mm_per_tooth, gear_big_count, gear_height, 0);
        
        for(i = [0:4]) {
            hull() {
                translate([
                    distant_filter_center_min * cos(i*72),
                    distant_filter_center_min * sin(i*72),
                    -20])
                cylinder(d = diam_filter, h = 40,$fn=256);
                translate([
                    distant_filter_center_max * cos(i*72),
                    distant_filter_center_max * sin(i*72),
                    -20])
                cylinder(d = diam_filter, h = 40,$fn=256);
            }
        }
        translate([0,0,-20])
        cylinder(r = gear_inner_radius, h = 40,$fn=128);
    }
}





module StepMotor28BYJSurPatte() {
    translate([8 - 7.8, 35/2, 10])
    StepMotor28BYJ();
}
// Le moteur
*union() {
translate([accroche_moteur_x, accroche_moteur_y, low_width + stepper_height])
    rotate([0,0,stepper_angle - 90])
    StepMotor28BYJSurPatte();
}


// La petite roue
*color("pink") {
translate([accroche_moteur_x,accroche_moteur_y, 0])
    rotate([0, 0, stepper_angle])
    translate([35/2,-8, 0])
    {
        small_gear_min = low_width + 1.2;
        small_gear_max = level_carroussel - 0.4;
        small_gear_cylinder_max= low_width + stepper_height - 2.0;
        small_gear_height = small_gear_max - small_gear_min;
        
        difference() {
            translate([0, 0, small_gear_min])
            {
                translate([0, 0, small_gear_height / 2])
                gear(gear_mm_per_tooth, gear_small_count, small_gear_height, 0);    
                cylinder(d = 11.4, h = small_gear_cylinder_max - small_gear_min, $fn=64);
            }
            // L'axe plein
            translate([0, 0, low_width + stepper_height - 4.6])
            cylinder(d=stepper_axe_diam, $fn=128, h=100);
            
            // Le méplat
            intersection() {
                cylinder(d=stepper_axe_diam, $fn=128, h=100);
                cube([20, stepper_axe_meplat, 200], center=true);
            }
        }
    };

}

// Le bas
*color("green") {
difference() {
    intersection() {
        corps();
        translate([-200, -200,0])
        cube([400,400,stepper_height + low_width]);
    }
    
    // Un espace pour faire passer la roue
    // 0.4 = liberté du carroussel (eviter de frotter)
    translate([0, 0, low_width - 1])
    cylinder(r = ray_carroussel + marge_carroussel,
             h = 100,
             $fn=256);
    
    // Un trou pour la premiere visse de fixation
    translate([accroche_moteur_x,accroche_moteur_y,-10])
    cylinder(d=m3_diam, $fn=64, h=100);
    // De la place pour la tete de la 1ere visse de fixation
    translate([accroche_moteur_x,accroche_moteur_y,low_width - 0.1])
    cylinder(d=m3_tete_diam, $fn=64, h=m3_tete_length + 0.2);
    
    
    
    // La place pour le moteur... on fait tout tourner
    translate([accroche_moteur_x,accroche_moteur_y, 0])
    for(vi = [-stepper_angle_step : stepper_angle_step])
        rotate([0, 0, stepper_angle + stepper_angle_tol * vi / stepper_angle_step])
        {
            // De la place pour la petite roue
            translate([35/2, -8, low_width + 0.6])
            cylinder(d=22, h=100, $fn=64);
            
            // De la place pour l'axe
            translate([35/2, -8, level_carroussel - 0.4])
            cylinder(d=6, h=100, $fn=64);
            
            // La visse de fixation
            translate([35, 0, -10])
            cylinder(d=m3_diam, $fn=64, h=100);
            
            // De la place pour la tete de la visse de fixation
            translate([35, 0, low_width - 0.1])
            cylinder(d=m3_tete_diam, $fn=64, h=m3_tete_length + 0.2);
            
        }

    for(accroche_pt = accroches) {
        translate([accroche_pt[0], accroche_pt[1], -10])
            cylinder(d=m3_diam, $fn=64, h=100);
    }
    
    // Les visses du capot
    for(accroche_pt = accroches_capot) {
        translate([accroche_pt[0], accroche_pt[1],low_width + 6]) {
            cylinder(d=m3_diam, $fn=64, h=100);
            
        }
        profondeur_ecrou = 2;
        translate([accroche_pt[0], accroche_pt[1],stepper_height + low_width - profondeur_ecrou]) {
            translate([0,0,(profondeur_ecrou+0.5)/2])
            hexagon(m3_ecrou_size, profondeur_ecrou + 0.5);    
        }
        
    }
}

}

// Le haut
*rotate([180,0,0])
color("cyan")
difference() {
    union() {
        intersection() {
            corps();
            translate([-200, -200,stepper_height + low_width + 0.1])
            cube([400,400,hauteur_externe + debord_hauteur - (stepper_height + low_width) - 0.1]);
        }
        
        
        // Le support pour le hall (sert juste de guide, le hall devra être collé)
        
        translate([
                0,
                -magnet_center_distance,
                sensor_low_level
                ])
        rotate([0, 0, opening_angle])
        difference() {
            hull() {
                intersection() {
                    cylinder(d=indicateur_diam_ext - 0.4,h=hauteur_externe + debord_hauteur - sensor_low_level, $fn=64);
                    translate([0,50,0])
                    cube([100,100,100], center=true);
                }
                    
                translate([0, -magnet_hall_support_width, 0])
                translate([-(indicateur_diam_ext - 0.4)/2,0,0])
                cube([indicateur_diam_ext - 0.4, 0.1, hauteur_externe + debord_hauteur - sensor_low_level]);
            }
            rotate([0,0,-90])
            translate([-3.6/2, -4.7/2, -0.1])
            cube([10, 4.7, 1.5 + 0.2]);
        }
        
        // Le support pour le hall (sert juste de guide, le hall devra être collé)
        /*translate([
                0,
        -magnet_center_distance,
                
                level_carroussel + width_carroussel + gear_height + magnet_hall_tolerance + 0.6])
        rotate([0,0,180])
        difference() {
            translate([-4.4/2, -5.5/2, 0])
            cube([10, 5.5, 5]);
            
            translate([-3.6/2, -4.7/2, -0.1])
            cube([10, 4.7, 1.5 - 0.6 + 0.1]);
        }*/
    }
    
    // Les trous pour le senseur hall (2mm, de quoi faire une soudure...
    translate([0,
                -magnet_center_distance,
                0])
    {
        rotate([0, 0, opening_angle]) {
            tol_x = 0.6;
            tol_y = 0.1;
             translate([0, -magnet_hall_support_width - 2, 0])
             cube([0.7 + tol_x, 0.7 + tol_y, 100], center = true);
             
             translate([-1.27, -magnet_hall_support_width, 0])
             cube([0.7 + tol_x, 0.7 + tol_y, 100], center = true);

             translate([1.27, -magnet_hall_support_width, 0])
             cube([0.7 + tol_x, 0.7 + tol_y, 100], center = true);

        }
    }
    
    // Les visses du capot
    for(accroche_pt = accroches_capot) {
        translate([accroche_pt[0], accroche_pt[1],0]) {
            cylinder(d=m3_diam, $fn=64, h=100);
            translate([0,0, stepper_height + low_width])
            hexagon(m3_ecrou_size, 0.2 + m3_ecrou_length);
        }
    }

    
    // La place pour le moteur... on fait tout tourner
    translate([accroche_moteur_x,accroche_moteur_y, 0])
    for(vi = [-stepper_angle_step : stepper_angle_step])
        rotate([0, 0, stepper_angle + stepper_angle_tol * vi / stepper_angle_step])
        {
            translate([35/2, 0, 0])
            cylinder(d=30, h=100, $fn=64);
            
            hull() {
                translate([0, 0, 0])
                cylinder(d=8, h=100, $fn=64);
            
                translate([35, 0, 0])
                cylinder(d=8, h=100, $fn=64);
            }
            
            translate([35/2 - 17 / 2, 12, 0])
            cube([17,7,100]);
        }
        
    
    for(accroche_pt = accroches) {
        translate([accroche_pt[0], accroche_pt[1], -10])
            cylinder(d=m3_diam, $fn=64, h=100);
    }   
};



// Le capteur a effet hall
*translate([0,
            -magnet_center_distance, 
            sensor_low_level])
rotate([0, 0, opening_angle])
translate([0,0,0.8]) // Avoir le senseur à partir de 0
rotate([180, 0, 0])
hallSensor();


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

module din8pos()
{
    rotate([0,0, opening_angle])
    translate([corps_y0, -39, hauteur_externe+debord_hauteur])
    translate([0, 0, din_face_x])
    rotate([0, 90, 0])
    rotate([180,0, 0])
    translate([0, 0, -din_oreille_z_level])
    children();
}

/*
*rotate([180,0,0])
rotate([0,0, opening_angle])
translate([corps_y0, -39, hauteur_externe+debord_hauteur])
translate([0, 0, din_face_x])
rotate([0, 90, 0])
rotate([180,0, 0])
translate([0, 0, -din_oreille_z_level])
*/
*rotate([180,0,0])
din8pos()
din8();

// Prise minidin8p
//*translate([40,-68,hauteur_externe + debord_hauteur])
//rotate([0,0,70])
//translate([-12/2,0,0])
//cube([12,13,12]);


module capot_clip()
{
    translate([-200,-200,hauteur_externe+debord_hauteur + 0.1])
    cube([400, 400, capot_height]);

}

// Le capot
rotate([180,0,0])
color("blue")
render()
difference() {
    union() {
        x_marge = capot_height - din_face_x;
        din8pos()
            translate([-x_marge, -3, 0.1])
            {
                cube([din_face_x + x_marge - 0.02, din_back_y + 6, din_z - 0.2]);
                cube([din_face_x + x_marge - 0.02, din_back_y + 9, 5.2]);
            };
        render()
        difference() {
            intersection() {
                corps();
                capot_clip();
            }
            union() {
                translate([0,0,hauteur_externe+debord_hauteur ])
                linear_extrude(height = capot_height - capot_width) {
                    offset(r=-capot_width)
                        projection() 
                        intersection() {
                            corps();
                            capot_clip();
                        }
                }
            }
        }
        intersection() {
            for(accroche_pt = accroches_capot) {
                translate([accroche_pt[0], accroche_pt[1], -10])
                cylinder(d=m3_diam + 3, $fn=64, h=100);
            
            }
            capot_clip();
        }
    }
    din8pos()
        din8();
    for(accroche_pt = accroches_capot) {
        translate([accroche_pt[0], accroche_pt[1], -10])
            cylinder(d=m3_diam, $fn=64, h=100);
    }
};

