package com.pcp.moviebooking.exception;

/** Unchecked exception: a movie, show, user or booking does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id " + id);
    }
}
