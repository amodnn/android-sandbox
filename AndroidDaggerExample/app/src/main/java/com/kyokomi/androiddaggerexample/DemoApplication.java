/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kyokomi.androiddaggerexample;

import android.app.Application;
import android.location.LocationManager;

import com.kyokomi.androiddaggerexample.ui.HomeActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;

public class DemoApplication extends Application {
  @Singleton
  @Component(modules = AndroidModule.class) interface ApplicationComponent {
    void inject(DemoApplication application);
    void inject(HomeActivity homeActivity);
    void inject(DemoActivity demoActivity);
  }
  
  @Inject LocationManager locationManager; // for some reason.
  
  private ApplicationComponent component;

  @Override public void onCreate() {
    super.onCreate();
    component = Dagger_DemoApplication$ApplicationComponent.builder()
        .androidModule(new AndroidModule(this))
        .build();
    component().inject(this); // As of now, LocationManager should be injected into this.
  }

  public ApplicationComponent component() {
    return component;
  }
}
