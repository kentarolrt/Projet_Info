# Projet Info GI4
# MedMap

## Présentation

MedMap est une application JavaFX développée dans le cadre du projet de programmation orientée objet.

Le projet permet de visualiser des centres médicaux sur une carte et d’analyser leur répartition à l’aide du diagramme de Voronoï et de la triangulation de Delaunay.

L’objectif est d’appliquer ces notions géométriques à une problématique concrète : la visualisation des déserts médicaux.

## Fonctionnalités principales

L’application permet de :

* afficher une carte de l’Île-de-France ;
* afficher des centres médicaux ;
* générer le diagramme de Voronoï associé aux centres ;
* afficher la triangulation de Delaunay ;
* ajouter un centre médical ;
* supprimer un centre médical ;
* déplacer un centre médical ;
* sauvegarder les centres médicaux ;
* générer des patients ;
* relier chaque patient au médecin le plus proche ;
* sélectionner un centre médical ;
* afficher des informations sur une zone ;
* visualiser les zones potentiellement moins bien couvertes.

## Technologies utilisées

* Java 21
* JavaFX
* Maven
* OpenStreetMap pour l’affichage de la carte
* Git / GitHub pour le travail collaboratif

## Structure du projet

Le projet est organisé en plusieurs packages :

* `com.example.medmap` : classes principales de lancement de l’application ;
* `com.example.medmap.map` : affichage de la carte, interactions utilisateur, zoom, patients et centres médicaux ;
* `com.example.medmap.model` : classes représentant les données du projet comme les médecins, patients et points ;
* `com.example.medmap.algo` : calculs liés au diagramme de Voronoï et à la triangulation de Delaunay ;
* `com.example.medmap.utils` : outils secondaires comme les logs console.

## Lancement du projet

Pour lancer l’application avec Maven :

```bash
mvn clean javafx:run
```

Le projet peut aussi être lancé depuis Eclipse avec :

```text
Run As > Maven build...
Goals : clean javafx:run
```

## Principe de fonctionnement

Les centres médicaux sont placés sur la carte sous forme de points. À partir de ces points, l’application calcule les zones de Voronoï, qui représentent les zones d’influence théoriques de chaque centre.

La triangulation de Delaunay permet de visualiser le maillage entre les différents centres médicaux.

Les patients générés sont automatiquement reliés au médecin le plus proche. Cela permet d’observer la répartition théorique de l’accès aux soins sur la zone étudiée.

## Limites du projet

Le projet est une simulation pédagogique. Les patients sont générés aléatoirement et ne correspondent pas à de vraies données démographiques.

Les distances sont calculées de manière simplifiée et ne prennent pas en compte les temps de trajet réels, les routes ou les transports.

Une amélioration possible serait d’intégrer des données réelles de population ou de santé, ainsi qu’un calcul d’accessibilité plus précis.

## Équipe

Projet réalisé par :

* Cyprien
* Kentaro
* Sulyvan
* Nathan
* Guirec
