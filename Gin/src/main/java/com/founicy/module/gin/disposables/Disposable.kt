package com.founicy.module.gin.disposables

/**
 *
 * A definition for canceling when works should be disposed.
 */
public interface Disposable {

  /** dispose the resource. */
  public fun dispose()

  /** returns true if this resource has been disposed. */
  public fun isDisposed(): Boolean
}
