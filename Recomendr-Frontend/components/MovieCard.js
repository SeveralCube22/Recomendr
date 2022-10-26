import React from 'react';
import { useState, useEffect } from "react";
import { View, Text, Image } from 'react-native';
import { Card, Button } from 'react-native-elements'

class Movie {
    constructor(title, director, actors, desc, img) {
        this.title = title;
        this.director = director;
        this.actors = actors;
        this.desc = desc;
        this.img = img;
    }
}

export default function MovieCard(props) {
    const movieId = props.id;
    let [movie, setMovie] = useState(null);

    useEffect(() => {
        if(!movie) {
            // Fetch movie details from id
            // set Movie
        }

    });

    return (
        <Card>
            <View style={styles.card}>
                <Image style={styles.image} source={{uri: (movie ? movie.img : "")}}></Image>
                <Card.Title> {movie ? movie.title : ""} </Card.Title>
            </View>
        </Card>
    );
}

const styles = StyleSheet().create({
    card: {
        flexDirection: 'row'
    },
    image: {

    }
});

/*

Maybe can abstract this out so that u can use books, and other type of content as well

 */

