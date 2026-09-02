package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.utils.PriceFormatter
import com.example.utils.SoundHelper

@Composable
fun ProductGridCard(
    product: Product,
    isFavorite: Boolean,
    onProductClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    currentQuantity: Int = 0,
    onIncrementQuantity: () -> Unit = onAddToCartClick,
    onDecrementQuantity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = {
                SoundHelper.playClickSound()
                onProductClick()
            })
            .testTag("product_card_${product.id}")
    ) {
        Column {
            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.nameAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount Badge
                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BunzoError)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = {
                        SoundHelper.playClickSound()
                        onFavoriteClick()
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .testTag("fav_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) BunzoError else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Category & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.categoryNameAr,
                        fontSize = 11.sp,
                        color = BunzoAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = BunzoAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${product.rating}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Product Name
                Text(
                    text = product.nameAr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = product.descriptionAr,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Price and Quantity / Add Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = PriceFormatter.formatPrice(product.price),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = BunzoPrimary
                        )
                        if (product.oldPrice != null) {
                            Text(
                                text = PriceFormatter.formatPrice(product.oldPrice),
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = Color.Gray
                            )
                        }
                    }

                    if (currentQuantity > 0) {
                        // Interactive Stepper directly on Card
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BunzoPrimary)
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    SoundHelper.playClickSound()
                                    onDecrementQuantity()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "إنقاص",
                                    tint = BunzoDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "$currentQuantity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BunzoDark,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    SoundHelper.playClickSound()
                                    onIncrementQuantity()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "زيادة",
                                    tint = BunzoDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = {
                                SoundHelper.playClickSound()
                                onAddToCartClick()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BunzoPrimary)
                                .testTag("add_to_cart_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة للسلة",
                                tint = BunzoDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductHorizontalCard(
    product: Product,
    onProductClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    currentQuantity: Int = 0,
    onIncrementQuantity: () -> Unit = onAddToCartClick,
    onDecrementQuantity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = {
                SoundHelper.playClickSound()
                onProductClick()
            })
            .testTag("product_h_card_${product.id}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.nameAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BunzoError)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.nameAr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.descriptionAr,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = PriceFormatter.formatPrice(product.price),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BunzoPrimary
                        )
                        if (product.oldPrice != null) {
                            Text(
                                text = PriceFormatter.formatPrice(product.oldPrice),
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = Color.Gray
                            )
                        }
                    }

                    if (currentQuantity > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(BunzoPrimary)
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    SoundHelper.playClickSound()
                                    onDecrementQuantity()
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "إنقاص",
                                    tint = BunzoDark,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "$currentQuantity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BunzoDark,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    SoundHelper.playClickSound()
                                    onIncrementQuantity()
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "زيادة",
                                    tint = BunzoDark,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = {
                                SoundHelper.playClickSound()
                                onAddToCartClick()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BunzoPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة",
                                tint = BunzoDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
