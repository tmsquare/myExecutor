TIMEFORMAT='CARS took: %R seconds.'
time {
curl https://perso.telecom-paristech.fr/eagan/class/igr204/data/cars.csv | grep Honda
}

TIMEFORMAT='FILMS took: %R seconds.'
time {
curl https://perso.telecom-paristech.fr/eagan/class/igr204/data/film.csv | grep John
}

TIMEFORMAT='SPEED DATING took: %R seconds.'
time {
curl https://perso.telecom-paristech.fr/eagan/class/igr204/data/SpeedDating.csv | grep Alabama
}

TIMEFORMAT='MAT took: %R seconds.'
time {
curl https://perso.telecom-paristech.fr/eagan/class/igr204/data/nat1900-2017.tsv | grep Test
}
