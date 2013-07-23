/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.classloader;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.classloader.mock.JavaIOFactory;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;
import org.jboss.forge.furnace.proxy.ClassLoaderAdapterBuilder;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClassLoaderAdapterJavaIOTest
{
   @Deployment(order = 3)
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addBeansXML()
               .addClasses(JavaIOFactory.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("dep", "1")
               )

               /*
                * Lightweight Service Container
                */
               .addAsServiceProvider(AddonLifecycleProvider.class, ServiceLoaderLifecycleProvider.class)
               .addAsServiceProvider(ServiceLoaderLifecycleProvider.SERVICE_REGISTRY_NAME,
                        ClassLoaderAdapterEnumCollisionsTest.class.getName())
               .addClasses(ServiceLoaderLifecycleProvider.class, ReflectionExportedInstance.class,
                        ReflectionServiceRegistry.class);

      return archive;
   }

   @Deployment(name = "dep,1", testable = false, order = 2)
   public static ForgeArchive getDeploymentDep1()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(JavaIOFactory.class)
               .addBeansXML();

      return archive;
   }

   @Test
   public void testSimpleFileProxy() throws Exception
   {
      AddonRegistry registry = ServiceLoaderLifecycleProvider.getFurnace(getClass().getClassLoader())
               .getAddonRegistry();
      ClassLoader thisLoader = ClassLoaderAdapterJavaIOTest.class.getClassLoader();
      ClassLoader dep1Loader = registry.getAddon(AddonId.from("dep", "1")).getClassLoader();

      Class<?> foreignType = dep1Loader.loadClass(JavaIOFactory.class.getName());
      File file = (File) foreignType.getMethod("getFile")
               .invoke(foreignType.newInstance());

      Assert.assertNotNull(file);
      Assert.assertTrue(file.getClass().equals(File.class));

      Object delegate = foreignType.newInstance();
      JavaIOFactory enhancedFactory = (JavaIOFactory) ClassLoaderAdapterBuilder.callingLoader(thisLoader)
               .delegateLoader(dep1Loader).enhance(delegate);

      Assert.assertTrue(Proxies.isForgeProxy(enhancedFactory));
      File result = enhancedFactory.getFile();
      Assert.assertFalse(Proxies.isForgeProxy(result));

      enhancedFactory.useFile(new File("foo"));
   }
}
