use <28BYJ-48.scad>;
use <publicDomainGearV1.1.scad>;
use <MCAD/shapes.scad>;


// LPO: 9/9/2015 16:18, ajout de 0.3 de marge
stepper_axe_meplat = 3.2+0.3;
stepper_axe_diam = 5.1+0.4;

// Ecrou de 3x30: http://www.leroymerlin.fr/v3/p/produits/lot-de-25-boulons-tete-cylindrique-en-acier-zingue-long-30-x-diam-3-mm-e125287
// Extrusion pour les ecrous de 3
m3_diam = 3.2 + 0.2;
// FIXME: verifier
m3_tete_diam = 5.5+0.4;
// FIXME : verifier
m3_tete_length = 2.5;
// http://www.visseriefixations.fr/ecrous/ecrous-autofreines/ecrou-hexagonal-autofreine-bague-nylon/ecrou-nylstop-inox-a2-din-985.html
// Prendre "s"
m3_ecrou_size = 5.5 + 0.3; // 6.01 / 2 + 0.15;

m3_ecrou_length = 2.5;

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
debord_width = 17;

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

diam_filter = 31.75 + 0.6;
distant_filter_center = ray_ext_max / 2; //31.75 / 2 + 20;
gear_mm_per_tooth=2.33;
gear_big_count = 145; // Il faudrait un multiple de 5 pour avoir des pièces identiques!
gear_small_count = 24;
gear_height = level_carroussel - low_width-0.8;
// Le rayon au centre de la grosse roue 
gear_inner_radius = 25;


indicateur_center_distance = opening_center_dist / cos(opening_angle);
indicateur_diam_ext = 8.4;
longueur_support_indicateur = 2.6;

// Le niveau bas du senseur
sensor_low_level = level_carroussel + width_carroussel + 1.4;
        
// On va surelever le senseur de ça (prévoir de la colle...)
magnet_hall_tolerance = 0.4;
magnet_center_distance = indicateur_center_distance;
magnet_diam = 4;
magnet_height = 2;


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
];

// Les accroches sur la RAF
accroches_main = [
    accroches[0], accroches[1]
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
stepper_angle_step = 5;

// http://www.conrad.fr/ce/fr/product/1303470/Embase-femelle-modulaire-RJ45-econ-connect-MEB88PST-embase-femelle-verticale-Ple-8-noir-1-pcs?ref=searchDetail
rj45_l = 15.88;
rj45_w = 15.36;
rj45_h = 16.38;
rj45_h_extra = rj45_h + 4;  // Avec une marge pour les fils
rj45_oreille_bas = 2; // A quelle distance du bas commence l'oreille
rj45_oreille_haut = 2; // A quelle distance du haut finit l'oreille
rj45_oreille_debord = 1.25; // quelle largeur en plus sur le connecteur rj45
rj45_oreille_recul = 2; // A quelle distance de la façade
rj45_oreille_largeur = 1; 


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
        translate([distant_filter_center, 0, -1]) 
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

module corps() {
    full_height = hauteur_externe + debord_hauteur + capot_height;
    y0 = -39.5;
    y1 = 38.5;
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
                cube([y1-y0, debord_width, debord_hauteur + capot_height /*- 0.2*/]);
            
        }
        
        rabot = 4;
        translate([ 
                0,
                -opening_center_dist - 0.2, 
                low_width +0.2])
        {
            difference() {
                cube([100,2*rabot,2*rabot], center =true);
                translate([0,-rabot,rabot])
                rotate([0,90,0])
                translate([0,0,-100])
                cylinder(r=rabot, h=200, $fn=32);
            }
            
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
            translate([
                distant_filter_center * cos(i*72),
                distant_filter_center * sin(i*72),
                -20])
            cylinder(d = diam_filter, h = 40,$fn=128);
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
union() {
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
        small_gear_min = low_width + 1.0;
        small_gear_max = level_carroussel - 0.4;
        small_gear_cylinder_max= low_width + stepper_height - 2.4;
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
        translate([accroche_pt[0], accroche_pt[1],stepper_height + low_width - 2.1]) {
            translate([0,0,2.2/2])
            hexagon(m3_ecrou_size, 2.2);
        }
    }
}

}

// Le haut
module haut() {
    color("green")
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
                    translate([0, -longueur_support_indicateur, 0])
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
            tol_x = 0.6;
            tol_y = 0.1;
            rotate([0, 0, opening_angle]) {
                 translate([0, -longueur_support_indicateur - 1.8, 0])
                 cube([0.7 + tol_x, 0.7 + tol_y, 100], center = true);
                 
                 translate([-1.27, -longueur_support_indicateur, 0])
                 cube([0.7 + tol_x, 0.7 + tol_y, 100], center = true);

                 translate([1.27, -longueur_support_indicateur, 0])
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
}


// Le capteur a effet hall
*translate([0,
            -magnet_center_distance, 
            sensor_low_level])
rotate([0, 0, opening_angle])
translate([0,0,0.8]) // Avoir le senseur à partir de 0
rotate([180, 0, 0])
hallSensor();

// Prise minidin8p
*translate([40,-68,hauteur_externe + debord_hauteur])
rotate([0,0,70])
translate([-12/2,0,0])
cube([12,13,12]);


module capot_clip()
{
    translate([-200,-200,hauteur_externe+debord_hauteur + 0.1])
    cube([400, 400, capot_height]);

}


// Le capot

module capot()
color("blue")
difference() {
    union() {
//        render()
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
            union() {
                for(accroche_pt = accroches_capot) {
                    translate([accroche_pt[0], accroche_pt[1], -10])
                    cylinder(d=m3_diam + 3, $fn=64, h=100);
            
                }
                for(accroche_pt = accroches_main) {
                    translate([accroche_pt[0], accroche_pt[1], -10])
                    rotate([0,0, opening_angle])
                    hexagon(m3_ecrou_size + 3, 200);
                }
                trou_rj45_place()
                trou_rj45_support();
            }
            capot_clip();
        }
        
        
        
    }
    for(accroche_pt = accroches_capot) {
        translate([accroche_pt[0], accroche_pt[1], -10])
            cylinder(d=m3_diam, $fn=64, h=100);
    }
    for(accroche_pt = accroches_main) {
        translate([accroche_pt[0], accroche_pt[1], hauteur_externe+debord_hauteur - 1])
        cylinder(d=m3_diam, $fn=64, h=6);
        
        translate([accroche_pt[0], accroche_pt[1], hauteur_externe+debord_hauteur+ 2.2/2])
        rotate([0,0, opening_angle])
        hexagon(m3_ecrou_size, 2.2);
    }
    trou_rj45_place()
    trou_rj45_retire();

};

rj45_trou_mur = 1.5;
// Positionne les pièce du trou rj45
module trou_rj45_place() 
{
        rotate([0,0, opening_angle])
        translate([38.5 -rj45_h_extra,-opening_center_dist+debord_width-0.2, hauteur_externe + debord_hauteur])
        scale([1,-1,1])
        children();
}

// Exteririeur du trou rj45
module trou_rj45_support()
{
                    cube([rj45_h_extra, 2 * rj45_trou_mur + 2 * rj45_oreille_debord + rj45_w,capot_height]);

}

// Extrusion pour le trou rj45
module trou_rj45_retire()
{
    mur = rj45_trou_mur;
    translate([rj45_h_extra - rj45_oreille_recul - rj45_oreille_largeur,mur,-capot_height + rj45_l])
    cube([rj45_oreille_largeur,2 * rj45_oreille_debord + rj45_w,capot_height]);
                
    translate([rj45_h_extra / 2, mur + rj45_oreille_debord, -capot_height + rj45_l])
    cube([rj45_h_extra / 2+1,rj45_w,capot_height]);
                
    translate([0 - 1, mur + rj45_oreille_debord, -capot_height + rj45_l + 1.4])
    cube([rj45_h_extra - rj45_h/2 + 2,rj45_w,capot_height]);
}

module prise_rj45()
{
    
    rotate([0,0, opening_angle])
    translate([38.5,-opening_center_dist-1-0.2, hauteur_externe + debord_hauteur + rj45_l])
    scale([1,-1,1])
    rotate([180,0,0])
    rotate([0,-90,0])
    {
        difference() {
            union() {
                cube([rj45_l, rj45_w, rj45_h]);
                translate([rj45_oreille_bas,-rj45_oreille_debord,rj45_oreille_recul])
                cube([rj45_l - rj45_oreille_bas - rj45_oreille_haut, rj45_w + 2 * rj45_oreille_debord, rj45_oreille_largeur]);
            }
            translate([4,1,-0.1])
            cube([rj45_l - 5, rj45_w - 2, rj45_h - 4]);
            translate([2,(rj45_w - 4 ) / 2,-0.1])
            cube([3, 4, rj45_h - 4]);
        }
        color("grey")
        for(i = [ 1,2,3,4]) {
            translate([-5,i * rj45_w / 5,rj45_h - 4])
            cube([5,0.7,0.7]);
        }
    }
}
//%render() 
capot();
//haut();

prise_rj45();